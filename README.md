# API-2021-SMTP

⚠️ Please clone [our own fork of MockMock server](https://github.com/HEIGVD-Course-API/MockMock) instead of the official one, because we resolved an issues with a dependency (see this [pull request](https://github.com/tweakers/MockMock/pull/8) if you want to have more information).

## Objectives

In this lab, you will develop a client application (TCP) in Java. This client application will use the Socket API to communicate with an SMTP server. The code that you write will include a **partial implementation of the SMTP protocol**. These are the objectives of the lab:

* Make practical experiments to become familiar with the **SMTP protocol**. After the lab, you should be able to use a command line tool to **communicate with an SMTP server**. You should be able to send well-formed messages to the server, in order to send emails to the address of your choice.

* Understand the notions of **test double** and **mock server**, which are useful when developing and testing a client-server application. During the lab, you will setup and use such a **mock server**.

* Understand what it means to **implement the SMTP protocol** and be able to send e-mail messages, by working directly on top of the Socket API (i.e. you are not allowed to use a SMTP library).

* **See how easy it is to send forged e-mails**, which appear to be sent by certain people but in reality are issued by malicious users.

* **Design a simple object-oriented model** to implement the functional requirements described in the next paragraph.


## Functional requirements

Your mission is to develop a client application that automatically plays pranks on a list of victims:

* The user should be able to **define a list of victims** (concretely, you should be able to create a file containing a list of e-mail addresses).
* The user should be able to **define how many groups of victims should be formed** in a given campaign. In every group of victims, there should be 1 sender and at least 2 recipients (i.e. the minimum size for a group is 3).
* The user should be able to **define a list of e-mail messages**. When a prank is played on a group of victims, then one of these messages should be selected. **The mail should be sent to all group recipients, from the address of the group sender**. In other words, the recipient victims should be lead to believe that the sender victim has sent them.

## Constraints

- The goal is for you to work at the wire protocol level (with the Socket API). Therefore, you CANNOT use a library that takes care of the protocol details. You have to work with the input and output streams.
- The program must be configurable: the addresses, groups, messages CANNOT be hard-coded in the program and MUST be managed in config files.


## Example

Consider that your program generates a group G1. The group sender is Bob. The group recipients are Alice, Claire and Peter. When the prank is played on group G1, then your program should pick one of the fake messages. It should communicate with an SMTP server, so that Alice, Claire and Peter receive an e-mail, which appears to be sent by Bob.

## Teams

You may work in teams of 2 students.

## Deliverables

You will deliver the results of your lab in a GitHub repository. You do not have to fork a specific repo, you can create one from scratch.

Your repository should contain both the source code of your Java project and your report. Your report should be a single `README.md` file, located at the root of your repository. The images should be placed in a `figures` directory.

Your report MUST include the following sections:

* **A brief description of your project**: if people exploring GitHub find your repo, without a prior knowledge of the API course, they should be able to understand what your repo is all about and whether they should look at it more closely.

* **Instructions for setting up a mock SMTP server (with Docker - which you will learn all about in the next 2 weeks)**. The user who wants to experiment with your tool but does not really want to send pranks immediately should be able to use a mock SMTP server. For people who are not familiar with this concept, explain it to them in simple terms. Explain which mock server you have used and how you have set it up.

* **Clear and simple instructions for configuring your tool and running a prank campaign**. If you do a good job, an external user should be able to clone your repo, edit a couple of files and send a batch of e-mails in less than 10 minutes.

* **A description of your implementation**: document the key aspects of your code. It is probably a good idea to start with a class diagram. Decide which classes you want to show (focus on the important ones) and describe their responsibilities in text. It is also certainly a good idea to include examples of dialogues between your client and an SMTP server (maybe you also want to include some screenshots here).

## References

* [Here is our fork of MockMock server](https://github.com/HEIGVD-Course-API/MockMock), in which we resolved an issues with a dependency (see this [pull request](https://github.com/tweakers/MockMock/pull/8) if you want to have more information).
* The [mailtrap](<https://mailtrap.io/>) online service for testing SMTP
* The [SMTP RFC](<https://tools.ietf.org/html/rfc5321#appendix-D>), and in particular the [example scenario](<https://tools.ietf.org/html/rfc5321#appendix-D>)
* Testing SMTP with TLS: `openssl s_client -connect smtp.mailtrap.io:2525 -starttls smtp -crlf`


## Description
Ce projet simule une campagne de fishing par email. 
Un serveur smtp est disponible à travers MockMock afin de ne pas générer de vrais mails.
Features:  
 - *définir une liste de victime* 
 - *définir des messages*, 
 
les messages sont envoyés par mail aux victimes indiqués.

## Getting started 
Prérequis:
Docker, maven et java 11.
1. Cloner le projet MockMock disponible ici :https://github.com/HEIGVD-Course-API/MockMock et ce projet
```bash
git clone https://github.com/HEIGVD-Course-API/MockMock
git clone https://github.com/BasileCuneo/API-2021-SMTP
```
2. Compiler les projets et déplacer l'archive MockMock dans le répertoire API
```bash
cd MockMock && mvn clean package
cd ../API-2021-SMTP && mvn clean package
cp ../MockMock/target/MockMock-1.4.0.one-jar.jar .
``` 
3. Construire l'image docker 
```bash
DOCKER_BUILDKIT=1 sudo docker build -t mockmock .
#veillez à ne pas déjà avoir une image avec ce tag
```
4. Lancer le serveur MockMock
```bash
sudo docker run -d -p 8282:8282 -p 2525:25 mockmock
```
5. lancer un navigateur web et aller sur http://localhost:8282/
6. Configurer les victimes (lire la partie configuration), si aucune configuration n'est faite le serveur utilise les victimes par défaut.
7. Configurer les messages (lire la partie configuration), si aucune configuration n'est faite le serveur utilise les messages par défaut.
8. Lancer le client qui génère les emails de phishing en lui donnant en argumant le nombre de groupe de victimes
```bash
java -jar target/ClientSMTP-1.0.jar  <nGroup>
```
9. Vérifier que les emails ont bien été envoyés en regardant votre navigateur web.

# Notes
Pour faire un vrai email et non pas contacter un serveur smtp fictif (mockmock), il faut changer l'adresse ip du server
smtp dans le code et remettre le port a 25 et non 2525.

## Configuration du projet
### Ajouter des messages
Le répertoire contenant les différents fichiers de messages est le répértoire API-2021-SMTP/src/configuration/messages
Chaque message est configuré à partir d'un fichier .txt, il faut donc créer un nouveau fichier pour chaque nouveau message.

Concernant le format des fichiers contenant des messages, la première ligne constitue le sujet et le reste du fichier le corps du message.

Exemple de fichier de message:

<début du fichier>Ceci est le sujet
Ceci est le corps du message

Ceci est toujours le corps du message et le retour à la ligne fonctionne<Fin du fichier>

### Ajouter des victimes
Le fichier contenant la liste des victimes est le fichier API-2021-SMTP/src/configuration/victims.txt

Pour y ajouter une victime, il suffit d'ajouter une nouvelle ligne avec l'adresse email voulue.
Les adresses doivent être correcte, i.e. avec un @ et un .
### Définir le nombre de groupes de victimes
Le nombre de groupes est à renseigner en argument de la ligne de commande lors du lancement du client.

En reprenant ce qui a été dit plus tôt, pour exécuter l'application client, il faut se rendre dans le répertoire API-2021-SMTP et utiliser la commande java -jar target/ClientSMTP-1.0.jar  <nGroup>

## Description et points clés de l'implémentation.

Notre implémentation est séparée en deux parties: le Client et le configManager.

### Client

Premièrement, le client va vérifier que le nombre de groupes a bien été saisi en argument de la ligne de commande.

Le client fera ensuite appel à la deuxième partie: le configManager afin de configurer les différentes fonctionnalités de l'applications.

Pour finir, le client met en place tout ce qui est nécessaire à la communication avec le serveur (socket, inputStream, outputStream) et fera appel à la méthode sendMail.

La méthode sendMail est basée sur la procédure utilisée pour envoyer un email via un serveur smtp.




