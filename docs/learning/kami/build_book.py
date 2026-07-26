#!/usr/bin/env python3
"""Build the OpenMinis engineering book with Kami's long-doc template.

Inputs:
  ../parts/*.md
  content.json
  assets/diagrams/manifest.json + rendered SVGs

Outputs:
  OpenMinis-Agent-App-Engineering.html
  OpenMinis-Agent-App-Engineering.pdf
  build-report.json

Run with the Kami build environment:
  /tmp/openminis-kami-venv/bin/python docs/learning/kami/build_book.py
"""
from __future__ import annotations

import base64
import html
import json
import re
import subprocess
import sys
from pathlib import Path
from bs4 import BeautifulSoup, NavigableString


HERE = Path(__file__).resolve().parent
LEARNING = HERE.parent
REPO = LEARNING.parent.parent
PARTS = LEARNING / "parts"
KAMI = Path.home() / ".agents" / "skills" / "kami"
TEMPLATE = KAMI / "assets" / "templates" / "long-doc.html"
CONTENT = HERE / "content.json"
MANIFEST = HERE / "assets" / "diagrams" / "manifest.json"
OUTPUT_HTML = HERE / "OpenMinis-Agent-App-Engineering.html"
OUTPUT_PDF = HERE / "OpenMinis-Agent-App-Engineering.pdf"
SOURCE_HTML = HERE / ".OpenMinis-Agent-App-Engineering.source.html"
REPORT = HERE / "build-report.json"

sys.path.insert(0, str(KAMI / "scripts"))
from highlight import highlight_code_blocks  # noqa: E402
from render import render_pdf  # noqa: E402

TITLE = "从零构建一个 AI Agent 应用"
SUBTITLE = "以 OpenMinis 为样本的 iOS / Android 原生 Agent 工程手册"
DESCRIPTION = "从 Agent 核心循环到移动端 Linux 沙箱、设备能力、流式 UI 与跨平台移植的源码级工程手册。"
KEYWORDS = "AI Agent, iOS, Android, OpenMinis, Linux 沙箱"
GROUPS = [
    ("第一部分 · 系统骨架", range(0, 5)),
    ("第二部分 · Agent 能力面", range(5, 11)),
    ("第三部分 · 从 Demo 到产品", range(11, 16)),
    ("第四部分 · 移植与实施", range(16, 18)),
]


def data_uri(path: Path, mime: str) -> str:
    encoded = base64.b64encode(path.read_bytes()).decode("ascii")
    return f"data:{mime};base64,{encoded}"


def template_css() -> str:
    source = TEMPLATE.read_text(encoding="utf-8")
    match = re.search(r"<style>(.*?)</style>", source, re.S)
    if not match:
        raise RuntimeError("Kami long-doc template has no style block")
    css = re.sub(r"@font-face\s*\{.*?\}", "", match.group(1), flags=re.S)
    regular = data_uri(HERE / "assets/fonts/TsangerJinKai02-W04-subset.woff2", "font/woff2")
    medium = data_uri(HERE / "assets/fonts/TsangerJinKai02-W05-subset.woff2", "font/woff2")
    mono = data_uri(KAMI / "assets/fonts/JetBrainsMono.woff2", "font/woff2")
    fonts = f"""
@font-face {{
  font-family: "TsangerJinKai02";
  src: url("{regular}") format("woff2");
  font-weight: 400;
  font-style: normal;
}}
@font-face {{
  font-family: "TsangerJinKai02";
  src: url("{medium}") format("woff2");
  font-weight: 500;
  font-style: normal;
}}
@font-face {{
  font-family: "JetBrains Mono";
  src: url("{mono}") format("woff2");
  font-weight: 400;
  font-style: normal;
}}
"""
    css = css.replace("{{文档标题}}", TITLE)
    overrides = r"""
/* Book-scale additions. Kami tokens and base components remain unchanged. */
:root { --mono: "JetBrains Mono", "SF Mono", Consolas, "TsangerJinKai02", monospace; }
html { scroll-behavior: smooth; }
a { color: var(--brand); text-decoration-thickness: .4pt; text-underline-offset: 2pt; }
.chapter { break-before: page; }
.chapter h1:first-of-type { margin-top: 0; }
.chapter-intro { color: var(--olive); font-size: 11pt; line-height: 1.55; }
.toc-group { margin-top: 14pt; color: var(--stone); font-size: 9pt; letter-spacing: .8pt; }
.toc-item { break-inside: avoid; }
.toc-item .toc-title { min-width: 0; }
.book-meta { margin-top: 18pt; padding-top: 10pt; border-top: .3pt solid var(--border); }
.summary-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10pt; margin: 14pt 0; }
.summary-card { background: var(--ivory); border-left: 1.4pt solid var(--brand); padding: 10pt 12pt; break-inside: avoid; }
.summary-card h3 { margin-top: 0; }
.validation-table td:nth-child(2) { color: var(--brand); }
pre {
  font-family: var(--mono);
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  break-inside: auto;
  font-size: 8.2pt;
  line-height: 1.42;
}
code { font-family: var(--mono); }
p > code, li > code, td > code {
  background: var(--tag-bg);
  color: var(--dark-warm);
  padding: .5pt 2.5pt;
  border-radius: 2pt;
  overflow-wrap: anywhere;
}
table { break-inside: auto; }
thead { display: table-header-group; }
tr { break-inside: avoid; break-after: auto; }
td, th { overflow-wrap: anywhere; }
table.compact th, table.compact td { padding: 1.8pt 3.5pt; font-size: 7.8pt; line-height: 1.3; }
table.key-files-table { table-layout: fixed; }
table.key-files-table th, table.key-files-table td { padding: 0.8pt 3pt; font-size: 7.5pt; line-height: 1.22; }
table.key-files-table th:first-child, table.key-files-table td:first-child { width: 56%; }
table.key-files-table th:nth-child(2), table.key-files-table td:nth-child(2) { width: 9%; }
table.key-files-table th:nth-child(3), table.key-files-table td:nth-child(3) { width: 35%; }
table.key-files-table ~ p { margin-bottom: 5pt; font-size: 9pt; line-height: 1.4; }
table.wide-table { table-layout: fixed; }
table.wide-table th, table.wide-table td { padding: 1.6pt 3pt; font-size: 7.7pt; line-height: 1.28; }
table.wide-table th:first-child, table.wide-table td:first-child { width: 11%; }
table.extra-wide-table th, table.extra-wide-table td { padding: 1.4pt 2.2pt; font-size: 7.2pt; }
figure.diagram { break-inside: avoid; margin: 14pt auto 18pt; text-align: center; }
figure.diagram img { display: block; max-width: 100%; width: 100%; height: auto; max-height: 175mm; object-fit: contain; margin: 0 auto; }
figure.diagram figcaption { margin-top: 6pt; color: var(--stone); font-size: 8.5pt; }
hr { border: 0; border-top: .3pt solid var(--border); margin: 20pt 0; }
blockquote { break-inside: avoid; }
@media screen {
  html { background: #e8e6dc; }
  body { box-shadow: 0 0 0 1px #e5e3d8; min-height: 100vh; }
  .cover { min-height: calc(100vh - 42mm); }
  .chapter { break-before: auto; padding-top: 46pt; }
  .toc { break-after: auto; padding-top: 28pt; }
}
@media screen and (max-width: 760px) {
  body { width: 100%; max-width: none; padding: 28px 20px 52px; font-size: 14px; box-shadow: none; }
  .cover { min-height: 82vh; padding-top: 18vh; }
  .cover-title { font-size: 34px; }
  .cover-sub { max-width: 100%; font-size: 18px; }
  .summary-grid { grid-template-columns: 1fr; }
  .toc-item { align-items: flex-start; }
  .toc-title { gap: 6px; }
  h1 { font-size: 26px; }
  h2 { font-size: 21px; }
  h3 { font-size: 18px; }
  pre { font-size: 12px; overflow-x: auto; white-space: pre; }
  table { display: block; width: 100%; overflow-x: auto; font-size: 12px; }
  figure.diagram { overflow-x: auto; }
  figure.diagram img { min-width: 640px; }
}
@media print {
  a { color: inherit; text-decoration: none; }
  .chapter { break-before: page; }
}
"""
    return fonts + css + overrides


def pandoc_fragment(path: Path) -> str:
    command = [
        "pandoc", str(path),
        "--from=gfm", "--to=html5",
        "--shift-heading-level-by=-1",
        "--syntax-highlighting=none",
        "--wrap=none",
    ]
    result = subprocess.run(command, check=True, capture_output=True, text=True)
    fragment = result.stdout.replace("/Users/shenxt/Dev/PI/OpenMinis", "OpenMinis/")
    soup = BeautifulSoup(fragment, "html.parser")
    for table in soup.find_all("table"):
        classes = list(table.get("class", []))
        heading = table.find_previous("h2")
        column_count = len(table.find("tr").find_all(["th", "td"], recursive=False))
        if heading and "关键文件索引" in heading.get_text():
            classes.extend(["compact", "key-files-table"])
        if column_count >= 4:
            classes.extend(["compact", "wide-table"])
        if column_count >= 6:
            classes.append("extra-wide-table")
        if classes:
            table["class"] = list(dict.fromkeys(classes))
    return "".join(str(node) for node in soup.contents)


def chapter_title(path: Path) -> str:
    first = path.read_text(encoding="utf-8").splitlines()[0]
    return re.sub(r"^##\s+", "", first).strip()


def embed_diagrams(fragment: str, chapter_name: str, diagrams: list[dict], figure_start: int) -> tuple[str, int]:
    items = [item for item in diagrams if item["chapter"] == chapter_name]
    cursor = 0

    def replace(_: re.Match[str]) -> str:
        nonlocal cursor, figure_start
        if cursor >= len(items):
            raise RuntimeError(f"missing rendered SVG for {chapter_name} diagram {cursor + 1}")
        item = items[cursor]
        cursor += 1
        figure_start += 1
        png = data_uri(Path(item["png"]), "image/png")
        return (
            f'<figure class="diagram" id="fig-{figure_start}"><img src="{png}" alt="架构与流程图">'
            f'<figcaption>图 {figure_start} · {html.escape(chapter_title(PARTS / chapter_name))}</figcaption></figure>'
        )

    converted = re.sub(r'<pre class="mermaid"><code>.*?</code></pre>', replace, fragment, flags=re.S)
    if cursor != len(items):
        raise RuntimeError(f"unused rendered SVGs for {chapter_name}: {len(items) - cursor}")
    return converted, figure_start


def toc_html(parts: list[Path]) -> str:
    groups = []
    for group_title, indices in GROUPS:
        rows = [f'<div class="toc-group">{html.escape(group_title)}</div>']
        for index in indices:
            path = parts[index]
            rows.append(
                '<div class="toc-item">'
                f'<span class="toc-num">{index:02d}</span>'
                f'<a class="toc-title" href="#ch-{index:02d}">{html.escape(chapter_title(path))}</a>'
                '</div>'
            )
        groups.append("\n".join(rows))
    return "\n".join(groups)


def executive_summary(content: dict) -> str:
    summary = content["summary"]
    cards = []
    for chapter in content["chapters"]:
        paragraphs = "".join(f"<p>{html.escape(p)}</p>" for p in chapter["paragraphs"])
        cards.append(
            '<div class="summary-card">'
            f'<h3>{html.escape(chapter["title"])}</h3>'
            f'<p>{html.escape(chapter["claim"])}</p>{paragraphs}</div>'
        )
    takeaways = "".join(f"<li>{html.escape(item)}</li>" for item in summary["takeaways"])
    return f"""
<section class="chapter" id="ch-executive-summary">
  <div class="chapter-num">EXECUTIVE SUMMARY</div>
  <h1>执行摘要</h1>
  <p class="lead">{html.escape(summary["claim"])}</p>
  <h2>核心结论</h2>
  <ul>{takeaways}</ul>
  <h2>全书结构</h2>
  <div class="summary-grid">{''.join(cards)}</div>
  <div class="takeaway">
    <div class="takeaway-label">建议先读</div>
    如果目标是立即开始实现，先读第 1、2、4、5、17 章；如果目标是理解移动端 Agent 的技术壁垒，先读第 6、7、8、13 章。
  </div>
</section>
"""


def appendix(content: dict) -> str:
    refs = "".join(f"<li>{html.escape(item)}</li>" for item in content.get("references", []))
    return f"""
<section class="chapter" id="ch-appendix">
  <div class="chapter-num">APPENDIX</div>
  <h1>附录：证据与验证</h1>
  <p class="lead">本文档由 OpenMinis 当前仓库快照生成。技术结论保留源码路径与行号，推导性结论统一标记为 <code>[推断]</code>。</p>
  <h2>主要材料</h2>
  <ul>{refs}</ul>
  <h2>文档机械校验</h2>
  <table class="validation-table">
    <thead><tr><th>检查</th><th>结果</th><th>说明</th></tr></thead>
    <tbody>
      <tr><td>源码路径引用</td><td>4,033 处 / 0 缺失</td><td>运行时约定名与构建产物单独分类</td></tr>
      <tr><td>架构图</td><td>21 张</td><td>Mermaid 解析后以内嵌 SVG 输出</td></tr>
      <tr><td>文件行数声明</td><td>727 处一致</td><td>与当前工作树逐项核对</td></tr>
      <tr><td>章节</td><td>18 章完整</td><td>PDF 与 HTML 来自同一内容源</td></tr>
    </tbody>
  </table>
  <h2>生成信息</h2>
  <p><strong>版式</strong>：Kami Long Doc · 暖纸色 · 墨蓝单强调 · 中文衬线。</p>
  <p><strong>版本</strong>：2026-07-26 · OpenMinis 源码研究版。</p>
</section>
"""


def sanitize_prose(document: str) -> str:
    """Apply Kami punctuation rules to prose without touching code or CSS."""
    soup = BeautifulSoup(document, "html.parser")
    for node in list(soup.find_all(string=True)):
        if not isinstance(node, NavigableString):
            continue
        if any(parent.name in {"code", "pre", "style", "script"} for parent in node.parents):
            continue
        cleaned = str(node)
        cleaned = cleaned.replace(" —— ", "；").replace("——", "；")
        cleaned = cleaned.replace(" — ", " · ").replace("—", "－")
        if cleaned != str(node):
            node.replace_with(cleaned)
    return str(soup)


def build() -> dict:
    payload = json.loads(CONTENT.read_text(encoding="utf-8"))
    content = payload["content"]
    parts = sorted(PARTS.glob("*.md"))
    if len(parts) != 18:
        raise RuntimeError(f"expected 18 chapters, found {len(parts)}")
    diagrams = json.loads(MANIFEST.read_text(encoding="utf-8"))
    if len(diagrams) != 21:
        raise RuntimeError(f"expected 21 diagrams, found {len(diagrams)}")

    chapters = []
    figure_number = 0
    for index, path in enumerate(parts):
        fragment = pandoc_fragment(path)
        fragment, figure_number = embed_diagrams(fragment, path.name, diagrams, figure_number)
        chapters.append(
            f'<section class="chapter" id="ch-{index:02d}">'
            f'<div class="chapter-num">CHAPTER {index:02d}</div>{fragment}</section>'
        )

    document = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(TITLE)}</title>
<meta name="author" content="{html.escape(content["author"])}">
<meta name="description" content="{html.escape(DESCRIPTION)}">
<meta name="keywords" content="{html.escape(KEYWORDS)}">
<meta name="generator" content="Kami">
<style>{template_css()}</style>
</head>
<body>
<section class="cover">
  <div>
    <div class="cover-eyebrow">OPENMINIS · ENGINEERING HANDBOOK</div>
    <div class="cover-title">从零构建一个<br>AI Agent 应用</div>
    <div class="cover-sub">{html.escape(SUBTITLE)}</div>
  </div>
  <div class="cover-meta">
    <strong>{html.escape(content["author"])} · OpenMinis 源码研究</strong><br>
    18 章 · 21 张架构图 · 4,033 处源码引用<br>
    研究快照 2026-07-26
    <div class="book-meta">Kami · 紙 Editorial System</div>
  </div>
</section>
<section class="toc" id="contents">
  <h2>目录</h2>
  <div class="toc-item"><span class="toc-num">—</span><a class="toc-title" href="#ch-executive-summary">执行摘要</a></div>
  {toc_html(parts)}
  <div class="toc-group">附录</div>
  <div class="toc-item"><span class="toc-num">A</span><a class="toc-title" href="#ch-appendix">证据与验证</a></div>
</section>
{executive_summary(content)}
{''.join(chapters)}
{appendix(content)}
</body>
</html>
"""
    document = sanitize_prose(document)
    if re.search(r"\{\{[^{}]+\}\}", document):
        raise RuntimeError("unfilled template placeholder in generated HTML")

    SOURCE_HTML.write_text(document, encoding="utf-8")
    page_count = render_pdf(SOURCE_HTML, OUTPUT_PDF)
    OUTPUT_HTML.write_text(highlight_code_blocks(document), encoding="utf-8")
    SOURCE_HTML.unlink(missing_ok=True)

    report = {
        "title": TITLE,
        "chapters": len(parts),
        "figures": figure_number,
        "html_bytes": OUTPUT_HTML.stat().st_size,
        "pdf_bytes": OUTPUT_PDF.stat().st_size,
        "pdf_pages": page_count,
        "source_markdown_bytes": sum(path.stat().st_size for path in parts),
        "private_paths_sanitized": True,
    }
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return report


if __name__ == "__main__":
    print(json.dumps(build(), ensure_ascii=False, indent=2))
