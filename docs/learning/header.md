# 从零构建一个 AI Agent 应用

## ——以 OpenMinis 为样本的移动端 Agent 工程手册

> **完整单文件归档**：约 1.25 MB，供下载、全文搜索和本地 Markdown 阅读器使用。
> 在线代码托管平台可能因文件过大而退化为纯文本预览；在线阅读请返回
> [`OpenMinis-Agent-App-Engineering.md`](OpenMinis-Agent-App-Engineering.md)，按章节打开。
>
> 研究对象：iOS (Swift/SwiftUI, ~187k 行) + Android (Kotlin/Compose, ~154k 行) 双端原生实现。
> 仓库快照日期：2026-07-26。每条技术论断标注真实源码路径与行号；推导结论标注 `[推断]`。

---

## 目录

| 章 | 标题 | 主题 |
|---|---|---|
| 0 | [序章：这本手册怎么用](#0-序章这本手册怎么用) | 规模、追踪码、阅读路径 |
| 1 | [整体架构与分层设计](#1-整体架构与分层设计) | 启动链路、分层、端到端数据流 |
| 2 | [Agent 核心循环](#2-agent-核心循环从一条消息到多轮工具调用) | 状态机、SSE、工具编排、取消 |
| 3 | [上下文工程](#3-上下文工程压缩预算与记忆) | token、压缩、memory、system prompt |
| 4 | [多模型 Provider 抽象层与认证](#4-多模型-provider-抽象层与认证) | 协议、wire 转换、OAuth |
| 5 | [工具系统](#5-工具系统定义分发权限与结果回灌) | schema、文件工具、权限门 |
| 6 | [iOS 沙箱：iSH](#6-ios-沙箱把-linux-塞进-app-进程ish) | 指令模拟、fakefs、rootfs |
| 7 | [Android 沙箱：PRoot](#7-android-沙箱proot-用户态-chroot) | ptrace、PTY 桥、挂载 |
| 8 | [Native Offload](#8-native-offload让沙箱里的-agent-调用设备能力) | IPC、能力清单、权限 |
| 9 | [Skills、Soul、Memory 与 MCP](#9-skillssoulmemory-与-mcp可扩展的-agent-能力面) | 渐进式加载、MCP 集成 |
| 10 | [浏览器自动化](#10-浏览器自动化让-agent-用你的登录态上网) | 动作集、页面表示、Cookie |
| 11 | [持久化与多端同步](#11-持久化与多端同步) | 存储模型、冲突解决、传输 |
| 12 | [UI 层与流式渲染](#12-ui-层与流式渲染让-8000-行的对话不掉帧) | 虚拟化、增量 markdown、数学 |
| 13 | [系统集成](#13-系统集成让-agent-长在操作系统里) | URL scheme、后台执行、Intents |
| 14 | [语音与多媒体管线](#14-语音与多媒体管线) | VAD/ASR、纠错、TTS、FFmpeg |
| 15 | [工程化：构建、调试、可观测](#15-工程化构建调试可观测与质量保障) | 依赖流水线、Debug RPC、测试 |
| 16 | [跨平台移植](#16-跨平台移植从-iosandroid-抽到-macoswindowslinux) | L1/L2/L3 分层、共享策略 |
| 17 | [从零开始的实施路线图](#17-从零开始的实施路线图) | 七阶段、验收动作、错误路线 |

---
