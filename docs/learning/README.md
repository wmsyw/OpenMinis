# OpenMinis 学习手册

对 OpenMinis 源码的逆向工程教学文档，面向"从零开发一个 iOS/Android AI Agent 应用，
并可拓展到 macOS/Windows/Linux"。

## 产物

- **`OpenMinis-Agent-App-Engineering.md`** — 合并后的完整手册（18 章）。这是给人读的那一份。
- `parts/` — 各章源文件，编号即章节号。改内容改这里。
- `header.md` — 标题页与目录。

改完任一章后重新生成合并文档：

```sh
sh docs/learning/assemble.sh
```

## 校验

四个脚本，都是纯 Python 3 标准库，无依赖。任何一个返回非零即为文档有错。

```sh
python3 docs/learning/verify_refs.py        # 引用的仓库路径真实存在
python3 docs/learning/verify_mermaid.py     # mermaid 图语法结构有效
python3 docs/learning/verify_linecounts.py  # 关键文件索引里声明的行数属实
python3 docs/learning/verify_toc.py         # 目录锚点可跳转（需先 assemble）
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
| `verify_refs` | 行内代码里形如 `src/…`、`deps/…` 的路径 | 构建产物（`deps/libs` 等）与运行时约定名（`SKILL.md`、`SOUL.md`）单列不计错；`src/android/.../Foo.kt` 省略写法用 glob 解析 |
| `verify_mermaid` | 围栏闭合、图类型已知、括号与引号配平 | 不做完整语法解析 |
| `verify_linecounts` | 章末索引表第二列的行数 vs `wc -l` | 100 行以下要求精确，以上允许 ±1% |
| `verify_toc` | `[…](#anchor)` vs `## ` 标题 | 复现 GitHub slugger |

## 写作约定

章节文件遵循统一结构，新增章节请照此办理：

1. 首行 `## N. 标题`（二级标题，全角冒号），章内层级从 `###` 起
2. `### N.1 这一章解决什么问题`
3. 中间为机制拆解，每处论断附 `` `路径:行号区间` `` 与精简真实代码片段
4. `### N.X 平台对照` — iOS vs Android 差异表
5. `### N.Y 设计取舍与踩坑`
6. `### N.Z 从零实现清单` — 有序列表，每步带验收标准
7. `### N.W 关键文件索引` — 表格 `| 路径 | 行数 | 职责 |`
8. 至少一张 mermaid 图
9. 未经证实的推断标注 `[推断]`

序章（00）与路线图（17）因性质不同，豁免第 4、5 条。
