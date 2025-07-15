#!/bin/bash

# EcoWiki 一键部署脚本 (Linux/macOS)
# 版本: 3.0

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

echo "===================================="
echo "      EcoWiki 一键部署脚本"
echo "===================================="
echo

# 检查MySQL是否可用
print_info "正在检查MySQL连接..."
if ! command -v mysql &> /dev/null; then
    print_error "未检测到MySQL，请确保MySQL已安装并在PATH中"
    exit 1
fi

print_success "MySQL检测成功！"
echo

# 设置数据库连接参数
read -p "请输入数据库主机地址 (默认: localhost): " DB_HOST
DB_HOST=${DB_HOST:-localhost}

read -p "请输入数据库端口 (默认: 3306): " DB_PORT
DB_PORT=${DB_PORT:-3306}

read -p "请输入数据库名称 (默认: ecowiki): " DB_NAME
DB_NAME=${DB_NAME:-ecowiki}

read -p "请输入数据库用户名 (默认: root): " DB_USER
DB_USER=${DB_USER:-root}

echo
echo "===================================="
echo "        数据库配置信息"
echo "===================================="
echo "主机: $DB_HOST"
echo "端口: $DB_PORT"
echo "数据库: $DB_NAME"
echo "用户: $DB_USER"
echo "===================================="
echo

read -p "确认以上配置是否正确？(y/n): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    print_warning "部署已取消"
    exit 0
fi

echo
print_info "正在连接数据库..."

# 检查数据库连接
read -s -p "请输入数据库密码: " DB_PASSWORD
echo

if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" >/dev/null 2>&1; then
    print_error "数据库连接失败，请检查连接参数和密码"
    exit 1
fi

print_success "数据库连接成功！"
echo

# 创建数据库（如果不存在）
print_info "正在创建数据库..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

print_success "数据库创建成功！"
echo

# 执行初始化脚本
print_info "正在执行数据库初始化..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < COMPLETE_DATABASE_INIT.sql

echo
print_success "数据库初始化完成！"
echo

# 更新后端配置文件
print_info "正在更新后端配置文件..."
cd www/backend/src/main/resources

# 创建本地配置文件
if [ ! -f application-local.properties ]; then
    print_info "创建 application-local.properties..."
    cat > application-local.properties << EOF
# EcoWiki 数据库配置
# 自动生成于 $(date)

# MySQL数据库配置
spring.datasource.url=jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
spring.datasource.username=$DB_USER
spring.datasource.password=$DB_PASSWORD

# 数据库连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
EOF
    
    print_success "配置文件已创建：www/backend/src/main/resources/application-local.properties"
else
    print_warning "配置文件已存在，跳过创建"
fi

cd ../../../../..

echo
echo "===================================="
echo "        部署完成！"
echo "===================================="
echo
print_info "数据库信息:"
echo "  数据库名: $DB_NAME"
echo "  主机地址: $DB_HOST:$DB_PORT"
echo
print_info "默认管理员账户:"
echo "  用户名: superadmin"
echo "  密码: EcoWiki@2025"
echo "  邮箱: admin@ecowiki.com"
echo
print_warning "重要提醒:"
echo "1. 请立即登录系统并修改默认密码"
echo "2. 数据库密码已自动配置到 application-local.properties"
echo "3. 确保后端配置正确后重启应用"
echo
print_info "启动后端服务命令:"
echo "  cd www/backend"
echo "  mvn spring-boot:run"
echo
print_info "启动前端服务命令:"
echo "  cd www/frontend"
echo "  npm run dev"
echo
echo "===================================="
