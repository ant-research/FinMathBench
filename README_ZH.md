# FinMathBench - 金融数学题库生成器

## 项目简介

FinMathBench是一个专门用于生成金融数学领域测试题目的Java项目。该项目能够基于预设的数学公式和参数，自动生成不同难度级别的金融数学题目，适用于金融模型评估、AI模型测试和教育培训等场景。

## 项目背景

随着金融科技的发展，对AI模型在金融数学领域能力的评估需求日益增长。FinMathBench项目旨在提供一个标准化的金融数学题库生成工具，帮助研究人员和开发者评估模型在金融计算、数学推理等方面的能力。

## 主要功能

- **公式管理**：支持L1和L2两个难度级别的数学公式定义和管理
- **题目生成**：基于预设公式自动生成单公式题目（N1）和多公式组合题目（N2-N4）
- **参数随机化**：为公式参数生成随机值，确保题目的多样性和有效性
- **题目验证**：自动验证生成题目的正确性和可计算性
- **多格式输出**：支持Excel格式的题目和答案输出

## 项目结构

```
FinMathBench/
├── Data-Generator/          # 核心数据生成模块
│   ├── src/main/java/com/alipay/FinMathBench/
│   │   ├── config/         # 配置类
│   │   ├── entity/         # 实体类（公式、题目等）
│   │   ├── task/           # 任务执行类
│   │   └── utils/          # 工具类
│   └── src/main/resources/
│       ├── prompt/         # 提示词模板
│       ├── paper_samples/  # 题目样本
│       └── *.xlsx          # 公式和题目数据文件
├── corpus-graph-log/       # 日志目录
└── README.md              # 项目说明文档
```

## 核心组件

### 实体类
- **Formula**: 数学公式表示，包含公式参数、表达式、约束条件等
- **Question**: 题目表示，包含题目文本、参数、答案等
- **FormulaField**: 公式字段，表示公式中的参数或结果
- **ActualValue**: 实际参数值，用于生成具体题目

### 任务类
- **QuestionBuilderTask**: 题目构建任务，负责生成不同难度级别的题目
- **FormulaBaseBuilderTask**: 公式库构建任务，负责构建基础公式库

### 工具类
- **LLMInvoker**: 大语言模型调用工具
- **ExcelReader/ExcelWriter**: Excel文件读写工具
- **ExpRunner**: 表达式计算工具

## 安装方法

### 环境要求
- Java 8 或更高版本
- Maven 3.6 或更高版本

### 编译安装
```bash
cd Data-Generator
mvn clean compile
```

## 使用方法

### 1. 配置LLM模型
在`com.alipay.FinMathBench.config.Config.java`中修改配置

### 2. 生成基础公式库
com.alipay.FinMathBench.task.FormulaBaseBuilderTask

### 3. 生成题目
com.alipay.FinMathBench.task.QuestionBuilderTask


## 输出文件

生成的题目将保存在以下文件中：
- `question_n1.xlsx`: 单公式题目（L1难度）
- `question_n2.xlsx`: 双公式组合题目
- `question_n3.xlsx`: 三公式组合题目
- `question_n4.xlsx`: 四公式组合题目

## 项目特性

- **多级别难度**：支持L1和L2两个难度级别的公式
- **智能组合**：能够将多个公式智能组合成复杂题目
- **参数验证**：自动验证生成题目的参数有效性
- **约束检查**：支持公式间的约束条件检查
- **灵活配置**：支持自定义提示词和生成参数