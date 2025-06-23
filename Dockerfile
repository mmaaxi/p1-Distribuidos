FROM openjdk:17-slim

WORKDIR /app

COPY . .
RUN javac src/Main.java -d .

CMD ["java", "Main"]
