## Общая информация по требованиям к окружению и конфигам
1. java 8
2. gradle
3. mysql 

В ```application-mysql.yml``` указываются данные базы, схема базы автоматически мигрирует
В ```application-mail.yml``` указываются данные для отправки почтовых сообщений

## Сборка и запуск локально
1. Актуализировать конфиг файлы (```application-mysql.yml``` -- для базы, ```application-mail.yml``` -- для почты)
1. выполнить команду ```gradle builde```
2. выполнить команду ```java -jar build/lib/volunteers.war```

## Отладка приложения
1. Достаточно открыть проект в ```IntelliJ IDEA```
2. запустить ```copyjs``` в грейдале
3. запустить класс ```ru.ifmo.neerc.volunteers.VolunteerApplication```

Для отладки файлов в ```webapp``` приложение перезапускать не надо

## Сборка релизной версии и раскатка
1. выполнить команду ```gradle build``` 
2. выполнить скрипт ```stripts/deploy.sh```
