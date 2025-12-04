# Оптимизация пересборки wallet-api

## Что было оптимизировано

1. **BuildKit cache mounts** - Gradle кэш теперь сохраняется между сборками
2. **Параллельная сборка** - включен `--parallel` для Gradle
3. **Build cache** - включен `--build-cache` для переиспользования артефактов
4. **Оптимизированный порядок слоев** - зависимости кэшируются отдельно от исходного кода
5. **Фиксированная версия Gradle** - используется `gradle:8-jdk21` вместо `latest`

## Быстрая пересборка

### Вариант 1: Использовать скрипт (рекомендуется)

```bash
cd docker-compose
./rebuild-wallet-api.sh
```

### Вариант 2: Вручную с BuildKit

```bash
cd docker-compose

# Включить BuildKit
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Пересобрать и запустить
docker compose build wallet-api
docker compose up -d wallet-api
```

### Вариант 3: Одной командой

```bash
cd docker-compose
DOCKER_BUILDKIT=1 COMPOSE_DOCKER_CLI_BUILD=1 docker compose up -d --build wallet-api
```

## Ожидаемое ускорение

- **Первая сборка**: без изменений (загружает зависимости)
- **Повторная сборка без изменений кода**: ~5-10 секунд (только финальный слой)
- **Сборка с изменением только исходного кода**: ~30-60% быстрее (зависимости из кэша)
- **Сборка с изменением зависимостей**: ~20-30% быстрее (частичный кэш)

## Дополнительные оптимизации

### 1. Использовать локальный Gradle daemon (для разработки)

Если вы работаете локально, можно использовать локальный Gradle daemon:

```bash
# В корне проекта
./gradlew :waltid-services:waltid-wallet-api:buildFatJar

# Затем просто скопировать JAR в контейнер
```

### 2. Очистка кэша (если что-то пошло не так)

```bash
# Очистить Docker build cache
docker builder prune

# Очистить только неиспользуемый кэш
docker builder prune -f

# Очистить все (осторожно!)
docker system prune -a
```

### 3. Мониторинг размера кэша

```bash
# Посмотреть использование дискового пространства
docker system df

# Детальная информация о кэше
docker builder du
```

## Troubleshooting

### BuildKit не работает

Убедитесь, что BuildKit включен:

```bash
# Проверить версию Docker
docker version

# Включить BuildKit глобально (опционально)
echo '{"features":{"buildkit":true}}' | sudo tee /etc/docker/daemon.json
sudo systemctl restart docker
```

### Кэш не работает

Проверьте, что используется правильный контекст сборки:

```bash
# Проверить, что контекст правильный
docker compose config | grep -A 5 wallet-api
```

### Медленная сборка

1. Проверьте, что `.dockerignore` исключает ненужные файлы
2. Убедитесь, что используется BuildKit (`DOCKER_BUILDKIT=1`)
3. Проверьте размер контекста: `du -sh .` в корне проекта

