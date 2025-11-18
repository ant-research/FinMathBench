# FinMathBench - Financial Mathematics Question Bank Generator

## Project Introduction

FinMathBench is a Java project specialized in generating test questions in the field of financial mathematics. This project can automatically generate financial mathematics questions of different difficulty levels based on preset mathematical formulas and parameters, suitable for financial model evaluation, AI model testing, and educational training scenarios.

## Project Background

With the development of financial technology, the demand for evaluating AI model capabilities in the field of financial mathematics is growing. The FinMathBench project aims to provide a standardized financial mathematics question bank generation tool to help researchers and developers evaluate model capabilities in financial calculations, mathematical reasoning, and other aspects.

## Main Features

- **Formula Management**: Support for mathematical formula definition and management at two difficulty levels (L1 and L2)
- **Question Generation**: Automatically generate single-formula questions (N1) and multi-formula combination questions (N2-N4) based on preset formulas
- **Parameter Randomization**: Generate random values for formula parameters to ensure question diversity and validity
- **Question Validation**: Automatically verify the correctness and computability of generated questions
- **Multi-format Output**: Support Excel format output for questions and answers

## Project Structure

```
FinMathBench/
├── Data-Generator/          # Core data generation module
│   ├── src/main/java/com/alipay/FinMathBench/
│   │   ├── config/         # Configuration classes
│   │   ├── entity/         # Entity classes (formulas, questions, etc.)
│   │   ├── task/           # Task execution classes
│   │   └── utils/          # Utility classes
│   └── src/main/resources/
│       ├── prompt/         # Prompt templates
│       ├── paper_samples/  # Question samples
│       └── *.xlsx          # Formula and question data files
├── corpus-graph-log/       # Log directory
└── README.md              # Project documentation
```

## Core Components

### Entity Classes
- **Formula**: Mathematical formula representation, containing formula parameters, expressions, constraints, etc.
- **Question**: Question representation, containing question text, parameters, answers, etc.
- **FormulaField**: Formula field, representing parameters or results in a formula
- **ActualValue**: Actual parameter values, used to generate specific questions

### Task Classes
- **QuestionBuilderTask**: Question building task, responsible for generating questions of different difficulty levels
- **FormulaBaseBuilderTask**: Formula library building task, responsible for building the basic formula library

### Utility Classes
- **LLMInvoker**: Large Language Model invocation tool
- **ExcelReader/ExcelWriter**: Excel file reading and writing tools
- **ExpRunner**: Expression calculation tool

## Installation Method

### Environment Requirements
- Java 8 or higher
- Maven 3.6 or higher

### Compile and Install
```bash
cd Data-Generator
mvn clean compile
```

## Usage Method

### 1. Configure LLM Model
Modify configuration in `com.alipay.FinMathBench.config.Config.java`

### 2. Generate Basic Formula Library
com.alipay.FinMathBench.task.FormulaBaseBuilderTask

### 3. Generate Questions
com.alipay.FinMathBench.task.QuestionBuilderTask


## Output Files

Generated questions will be saved in the following files:
- `question_n1.xlsx`: Single-formula questions (L1 difficulty)
- `question_n2.xlsx`: Double-formula combination questions
- `question_n3.xlsx`: Triple-formula combination questions
- `question_n4.xlsx`: Quadruple-formula combination questions

## Project Features

- **Multi-level Difficulty**: Support for two difficulty levels (L1 and L2) of formulas
- **Intelligent Combination**: Ability to intelligently combine multiple formulas into complex questions
- **Parameter Validation**: Automatically verify the validity of generated question parameters
- **Constraint Checking**: Support for constraint condition checking between formulas
- **Flexible Configuration**: Support for custom prompts and generation parameters