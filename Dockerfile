FROM openjdk:17-slim

WORKDIR /app

COPY . .

RUN javac Main.java

CMD ["java", "Main"]
