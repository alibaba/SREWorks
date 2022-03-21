while true
do
    cd /home/admin/git/tkg-one/tkg-one-start/src; 
    ~/oss.sh get tkg-one-start-src-code.tar.gz; 
    tar zxvf tkg-one-start-src-code.tar.gz; 
    rm -f /home/admin/git/tkg-one/tkg-one-start/src/main/java/com/alibaba/tesla/tkg-one-start-src-code.tar.gz
done
