# Решение проблем с пересборкой wallet-api

## Проблема: `docker compose up --force-recreate --build` не пересобирает

### Причины:

1. **Отсутствует секция `build`** в `docker-compose.yaml` - теперь исправлено ✅
2. **`pull_policy: missing`** - Docker может использовать существующий образ вместо пересборки
3. **Docker кэш** - Docker может использовать кэшированные слои

## Правильные команды для пересборки:

### Вариант 1: Полная пересборка без кэша (рекомендуется при проблемах)

```bash
cd docker-compose
docker compose build --no-cache wallet-api
docker compose up -d --force-recreate wallet-api
```

### Вариант 2: Пересборка с кэшем (быстрее)

```bash
cd docker-compose
docker compose build wallet-api
docker compose up -d --force-recreate wallet-api
```

### Вариант 3: Одной командой

```bash
cd docker-compose
docker compose up -d --build --force-recreate wallet-api
```

### Вариант 4: Использовать скрипт

```bash
cd docker-compose
./rebuild-wallet-api.sh
```

## Почему `--build` может не работать:

1. **Образ уже существует** - Docker может использовать существующий образ, если он соответствует тегу
2. **Кэш слоев** - Docker использует кэшированные слои, если файлы не изменились
3. **BuildKit не включен** - для лучшего кэширования нужен BuildKit

## Принудительная пересборка:

Если нужно гарантированно пересобрать:

```bash
# Удалить существующий образ
docker rmi waltid/wallet-api:latest 2>/dev/null || true

# Пересобрать без кэша
cd docker-compose
DOCKER_BUILDKIT=1 COMPOSE_DOCKER_CLI_BUILD=1 docker compose build --no-cache wallet-api

# Пересоздать и запустить
docker compose up -d --force-recreate wallet-api
```

## Проверка, что образ пересобран:

```bash
# Посмотреть время создания образа
docker images waltid/wallet-api

# Посмотреть логи сборки
docker compose build wallet-api

# Проверить, что контейнер использует новый образ
docker compose ps wallet-api
docker inspect <container_id> | grep Image
```

## Важные моменты:

1. **BuildKit должен быть включен** для оптимального кэширования:
   ```bash
   export DOCKER_BUILDKIT=1
   export COMPOSE_DOCKER_CLI_BUILD=1
   ```

2. **Секция `build` обязательна** в `docker-compose.yaml` для локальной сборки

3. **`pull_policy: missing`** означает, что Docker будет использовать локальный образ, если он есть, вместо pull из registry

4. **Для полной пересборки** используйте `--no-cache` флаг

