#! /bin/bash
timestamp=$(date +%s)

log="3-flash"
log=$log$timestamp
log=$log".log"

for ((i = 0; i < $1; i++)); do
   options=("1" "10" "5" "50" "100")
   RANDOM=$[ $i + $[ RANDOM % 10 ]]
   selectedoption=${options[$RANDOM % ${#options[@]} ]}

# java -jar ../../image_resizer_client/dist/image_resizer_client.jar -file ../images/image$selectedoption.zip -o $i.zip &

   java -jar ../../image_resizer_client/dist/image_resizer_client.jar -file ../images/image10.zip -o $i.zip &

done
exit 0
