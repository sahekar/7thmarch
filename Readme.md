This project provide the tranformation avaya events into Sessions

Project bases on Kafka streams


Build the project

./gradlew build



impromvents and problems:

- think about compatabiltity with new AvayaPacket json shemas. at this moment when we remove old field we will have problem with deserialisation this message
- need to find way of quicklies removing data from rocketsdb