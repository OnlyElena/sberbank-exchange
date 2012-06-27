#!/bin/bash

mvn package

if [ $? -eq 0 ] 
 then 
   scp sberbank-dao/sberbankExchage-jar-with-dependencies.jar root@10.38.12.13:/root/sputnik_cron/sberbank_exchage
   scp exProd.xml root@10.38.12.13:/root/sputnik_cron/sberbank_exchage
fi
