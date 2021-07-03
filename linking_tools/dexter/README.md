Dexter is a framework that implements some popular algorithms and provides all the tools needed to develop any entity linking technique.

It's source code is avaialbe at: https://github.com/dexter/dexter.
It does not offer any live api, so we have to deploy it locally.

### Run Dexter Locally
To start the dexter entity linking service, 
just download the model with the binaries:

```shell
wget http://hpc.isti.cnr.it/~ceccarelli/dexter2.tar.gz
tar -xvzf dexter2.tar.gz
cd dexter2
java -Xmx4000m -jar dexter-2.1.0.jar
```
It will start a service listening to port `8080`.
To check whether the service is available, 
run 
```
curl -XGET http://114.212.190.19:8080/dexter-webapp/api/rest/annotate?text=Bob%20Dylan%20and%20Johnny%20Cash%20had%20formed%20a%20mutual%20admiration%20society%20even%20before%20they%20met%20in%20the%20early%201960s&n=50&wn=false&debug=false&format=text&min-conf=0.5
``` 



For more information, please refer to the official repository of dexter: https://github.com/dexter/dexter

 


