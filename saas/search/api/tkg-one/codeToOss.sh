while true
do 
    cd /Users/jinghua.yjh/git/tkg-one/tkg-one-start/src;
    tar zcvf /tmp/tkg-one-start-src-code.tar.gz *; 
    ~/oss.sh put /tmp/tkg-one-start-src-code.tar.gz;
done
