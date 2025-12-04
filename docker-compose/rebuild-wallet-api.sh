#!/bin/bash
# Скрипт для быстрой пересборки wallet-api с использованием BuildKit

set -e

echo "🚀 Быстрая пересборка wallet-api..."

# Включаем BuildKit для лучшего кэширования
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Переходим в директорию docker-compose
cd "$(dirname "$0")"

# Пересобираем только wallet-api
echo "📦 Сборка образа wallet-api..."
docker compose build wallet-api

# Перезапускаем сервис
echo "🔄 Перезапуск wallet-api..."
docker compose up wallet-api

echo "✅ Готово! wallet-api пересобран и запущен."
echo "📊 Логи: docker compose logs -f wallet-api"

