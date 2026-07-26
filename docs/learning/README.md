# OpenMinis 学习手册

对 OpenMinis 源码的逆向工程教学文档，面向「从零开发一个 iOS / Android AI Agent 应用，并可拓展到 macOS / Windows / Linux」。

## 文档入口

| 文件 | 用途 | 在线预览 |
|---|---|---|
| **[`OpenMinis-Agent-App-Engineering.md`](OpenMinis-Agent-App-Engineering.md)** | 可渲染的总目录、阅读路线与章节导航 | 推荐 |
| [`parts/`](parts/) | 18 个独立章节，单章约 23–91 KB | 推荐 |
| `OpenMinis-Agent-App-Engineering-full.md` | 18 章合并归档，约 1.25 MB | 不推荐；下载后本地阅读 |
| [`kami/OpenMinis-Agent-App-Engineering.html`](kami/OpenMinis-Agent-App-Engineering.html) | Kami 排版的完整交互式 HTML 单文件，内嵌字体、图表与代码高亮 | 推荐；浏览器阅读 |
| [`kami/OpenMinis-Agent-App-Engineering.pdf`](kami/OpenMinis-Agent-App-Engineering.pdf) | Kami 排版的 457 页 PDF，适合离线阅读与打印 | 推荐；离线阅读 |
| `header.md` | 完整归档的标题页和内部目录 | — |

原先把 18 章全部放进 `OpenMinis-Agent-App-Engineering.md`。文件达到 1.25 MB 后，部分代码托管平台超过 Markdown 渲染阈值，会退化成纯文本预览，表现为标题、表格和段落全挤在一起。现在总入口保持在约 5 KB，只链接独立章节；完整单文件仅作为离线归档。

修改任一章节后重新生成离线归档：

```sh
sh docs/learning/assemble.sh
```

该命令只写 `OpenMinis-Agent-App-Engineering-full.md`，不会覆盖在线阅读入口。

## 校验

四个脚本都是纯 Python 3 标准库，无额外依赖。任何一个返回非零即表示文档有错。

```sh
python3 docs/learning/verify_refs.py        # 引用的仓库路径真实存在
python3 docs/learning/verify_mermaid.py     # Mermaid 图结构有效
python3 docs/learning/verify_linecounts.py  # 关键文件索引声明的行数属实
python3 docs/learning/verify_toc.py         # 入口章节链接和归档内部目录可达
```

一次跑完：

```sh
for s in refs mermaid linecounts toc; do
  printf '%-12s ' "$s"
  python3 docs/learning/verify_"$s".py >/dev/null && echo PASS || echo FAIL
done
```

各脚本的判定口径：

| 脚本 | 检查什么 | 容错 |
|---|---|---|
| `verify_refs` | 行内代码里形如 `src/…`、`deps/…` 的路径 | 构建产物与运行时约定名不计错；`src/android/.../Foo.kt` 用 glob 解析 |
| `verify_mermaid` | 围栏闭合、图类型、括号与引号配平 | 不做完整 Mermaid 语法解析 |
| `verify_linecounts` | 章末索引表第二列的行数 vs `wc -l` | 100 行以下要求精确，以上允许 ±1% |
| `verify_toc` | 总入口的章节文件链接；归档的内部标题锚点 | 复现 GitHub slugger |

## 写作约定

章节文件遵循统一结构：

1. 首行 `## N. 标题`，章内层级从 `###` 起
2. `### N.1 这一章解决什么问题`
3. 机制拆解附 `` `路径:行号区间` `` 与精简真实代码片段
4. `### N.X 平台对照` — iOS vs Android 差异表
5. `### N.Y 设计取舍与踩坑`
6. `### N.Z 从零实现清单` — 有序列表，每步带验收标准
7. `### N.W 关键文件索引` — 表格 `| 路径 | 行数 | 职责 |`
8. 至少一张 Mermaid 图
9. 未经源码直接证实的推断标记 `[推断]`

序章（00）与路线图（17）因文体不同，豁免第 4、5 条。
