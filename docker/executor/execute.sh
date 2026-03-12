# ==================== Java 代码执行脚本 ====================
# 这个脚本负责：
# 1. 编译 Java 代码
# 2. 运行 Java 代码
# 3. 捕获输出和错误
# 4. 限制执行时间和资源
# ==================== 配置部分 ====================
# 从环境变量读取配置，如果没有则使用默认值
TIMEOUT=${TIMEOUT:-10}           # 超时时间（秒）
MEMORY_LIMIT=${MEMORY_LIMIT:-128m}  # 内存限制
CODE_DIR="/app/code"             # 代码目录
OUTPUT_DIR="/app/output"         # 输出目录
TEMP_DIR="/app/temp"             # 临时目录
# ==================== 颜色输出（方便调试）====================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
# ==================== 日志函数 ====================
# 这些函数让输出更易读
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}
log_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}
log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}
# ==================== 清理函数 ====================
# 执行完毕后清理临时文件
cleanup() {
    log_info "Cleaning up temporary files..."
    rm -rf "$TEMP_DIR"/*
    # 注意：不清理 CODE_DIR，因为可能需要查看代码
}
# ==================== 设置退出时清理 ====================
# trap 命令：在脚本退出时自动执行 cleanup 函数
# 无论脚本是正常退出还是异常退出，都会执行清理
trap cleanup EXIT
# ==================== 检查代码文件 ====================
# 检查是否存在 Main.java 文件
log_info "Checking for code files..."
if [ ! -f "$CODE_DIR/Main.java" ]; then
    log_error "No Main.java found in $CODE_DIR"
    log_info "Please mount your code to $CODE_DIR/Main.java"
    exit 1
fi
log_info "Found Main.java"
# ==================== 显示代码内容（调试用）====================
log_info "Code content:"
echo "----------------------------------------"
cat "$CODE_DIR/Main.java"
echo "----------------------------------------"
# ==================== 编译代码 ====================
log_info "Compiling Java code..."
cd "$CODE_DIR"
# 编译代码，错误输出到临时文件
if ! javac Main.java 2> "$TEMP_DIR/compile_errors.txt"; then
    log_error "Compilation failed!"
    echo "=== Compilation Errors ==="
    cat "$TEMP_DIR/compile_errors.txt"
    echo "=== End of Errors ==="
    
    # 将错误信息写入输出文件
    echo "COMPILE_ERROR=true" > "$OUTPUT_DIR/metadata.txt"
    cat "$TEMP_DIR/compile_errors.txt" > "$OUTPUT_DIR/error.txt"
    
    exit 1
fi
log_info "Compilation successful!"
# ==================== 检查是否有输入文件 ====================
INPUT_FILE=""
if [ -f "$CODE_DIR/input.txt" ]; then
    INPUT_FILE="$CODE_DIR/input.txt"
    log_info "Found input file, will use it for execution"
fi
# ==================== 运行代码 ====================
log_info "Executing Java code..."
log_info "Timeout: ${TIMEOUT}s, Memory: ${MEMORY_LIMIT}"
# 记录开始时间
START_TIME=$(date +%s%N)
# 运行代码
# timeout: 超时控制
# -Xmx: 最大堆内存
# -Xms: 初始堆内存
if [ -n "$INPUT_FILE" ]; then
    # 有输入文件
    timeout "$TIMEOUT" java -Xmx"$MEMORY_LIMIT" -Xms64m Main < "$INPUT_FILE" > "$OUTPUT_DIR/output.txt" 2> "$TEMP_DIR/runtime_errors.txt"
else
    # 没有输入文件
    timeout "$TIMEOUT" java -Xmx"$MEMORY_LIMIT" -Xms64m Main > "$OUTPUT_DIR/output.txt" 2> "$TEMP_DIR/runtime_errors.txt"
fi
# 捕获退出码
EXIT_CODE=$?
# 记录结束时间
END_TIME=$(date +%s%N)
# 计算执行时间（毫秒）
EXECUTION_TIME=$(( (END_TIME - START_TIME) / 1000000 ))
# ==================== 处理执行结果 ====================
if [ $EXIT_CODE -eq 0 ]; then
    # 执行成功
    log_info "Execution successful!"
    log_info "Execution time: ${EXECUTION_TIME}ms"
    
    # 输出结果
    echo "=== OUTPUT ==="
    cat "$OUTPUT_DIR/output.txt"
    echo "=== END ==="
    
    # 写入元数据
    echo "SUCCESS=true" > "$OUTPUT_DIR/metadata.txt"
    echo "EXECUTION_TIME=$EXECUTION_TIME" >> "$OUTPUT_DIR/metadata.txt"
    
    exit 0
    
elif [ $EXIT_CODE -eq 124 ]; then
    # 超时
    log_error "Execution timed out after ${TIMEOUT}s!"
    
    echo "TIMEOUT=true" > "$OUTPUT_DIR/metadata.txt"
    echo "EXECUTION_TIME=$EXECUTION_TIME" >> "$OUTPUT_DIR/metadata.txt"
    
    exit 124
    
else
    # 其他错误
    log_error "Execution failed with exit code: $EXIT_CODE"
    
    echo "RUNTIME_ERROR=true" > "$OUTPUT_DIR/metadata.txt"
    echo "EXIT_CODE=$EXIT_CODE" >> "$OUTPUT_DIR/metadata.txt"
    echo "EXECUTION_TIME=$EXECUTION_TIME" >> "$OUTPUT_DIR/metadata.txt"
    
    # 输出错误信息
    if [ -s "$TEMP_DIR/runtime_errors.txt" ]; then
        echo "=== Runtime Errors ==="
        cat "$TEMP_DIR/runtime_errors.txt"
        echo "=== End of Errors ==="
        cat "$TEMP_DIR/runtime_errors.txt" > "$OUTPUT_DIR/error.txt"
    fi
    
    exit $EXIT_CODE
fi