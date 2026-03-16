#!/bin/bash

# 后端容器测试脚本
# 测试各个API端点

BASE_URL="http://localhost:8080/api/v1"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "🧪 后端容器 API 测试"
echo "========================================"
echo ""

# 1. 测试根路径
echo -e "${YELLOW}[测试 1] 根路径 GET /api/v1${NC}"
curl -s -X GET "$BASE_URL" | jq .
echo ""
echo ""

# 2. 测试健康检查
echo -e "${YELLOW}[测试 2] 健康检查 GET /api/v1/health${NC}"
curl -s -X GET "$BASE_URL/health" | jq .
echo ""
echo ""

# 3. 测试代码提交（简单Java代码）
echo -e "${YELLOW}[测试 3] 提交代码评审任务 POST /api/v1/code/submit${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/code/submit" \
  -H "Content-Type: application/json" \
  -d '{
    "className": "TestCode",
    "code": "public class TestCode {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}"
  }')

echo "$RESPONSE" | jq .

# 提取 taskId
TASK_ID=$(echo "$RESPONSE" | jq -r '.data.taskId')
echo ""
echo -e "${GREEN}任务ID: $TASK_ID${NC}"
echo ""

# 4. 检查任务状态
echo -e "${YELLOW}[测试 4] 检查任务状态 GET /api/v1/code/status/$TASK_ID${NC}"
curl -s -X GET "$BASE_URL/code/status/$TASK_ID" | jq .
echo ""
echo ""

# 5. 等待几秒后查询结果
echo -e "${YELLOW}等待 5 秒后查询结果...${NC}"
sleep 5

echo -e "${YELLOW}[测试 5] 查询任务结果 GET /api/v1/code/result/$TASK_ID${NC}"
curl -s -X GET "$BASE_URL/code/result/$TASK_ID" | jq .
echo ""
echo ""

# 6. 测试错误情况 - 空代码
echo -e "${YELLOW}[测试 6] 测试错误情况 - 空代码${NC}"
curl -s -X POST "$BASE_URL/code/submit" \
  -H "Content-Type: application/json" \
  -d '{
    "className": "TestCode",
    "code": ""
  }' | jq .
echo ""
echo ""

# 7. 测试错误情况 - 不存在的任务
echo -e "${YELLOW}[测试 7] 测试错误情况 - 不存在的任务${NC}"
curl -s -X GET "$BASE_URL/code/result/non-existent-task-id" | jq .
echo ""
echo ""

echo "========================================"
echo -e "${GREEN}✅ 测试完成${NC}"
echo "========================================"
