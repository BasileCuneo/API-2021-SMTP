# Instructions 

## Pour build : 
DOCKER_BUILDKIT=1 sudo docker build -t mockmock .

## Pour run 
sudo docker run -d -p 8282:8282 -p 2525:25 mck

## Pour utiliser le programme avec l'outil de spam, changer le port dans le code pour utiliser le port 2525 et faire` mvn exec:java`
