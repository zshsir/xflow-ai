#!/bin/bash
# 数据库备份脚本

set -e

BACKUP_DIR="/opt/xflow/backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# 加载环境变量
source /opt/xflow/.env

mkdir -p $BACKUP_DIR

# 备份数据库
echo "Starting backup at $(date)..."
docker exec xflow-mysql mysqldump \
    -uroot \
    -p"$MYSQL_ROOT_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    "$MYSQL_DATABASE" | gzip > "$BACKUP_DIR/db_${DATE}.sql.gz"

# 检查备份是否成功
if [ $? -eq 0 ] && [ -s "$BACKUP_DIR/db_${DATE}.sql.gz" ]; then
    echo "Backup successful: db_${DATE}.sql.gz"
    echo "Size: $(du -h $BACKUP_DIR/db_${DATE}.sql.gz | cut -f1)"
else
    echo "Backup FAILED!"
    exit 1
fi

# 删除旧备份
echo "Cleaning up old backups (older than $RETENTION_DAYS days)..."
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed at $(date)"
