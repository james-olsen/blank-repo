# API Testing Framework

### APIClientTester.java 
The API client tester has already been instantiated as a variable called client.
Currently the client can handle basic HTTP requests and we are able to set header fields, cookies, and a payload in the case of POST and PUT requests.
The API will work and will not verify the response given, but the user can access and verify response codes, descriptors, headers, cookies, and the response body to then handle their own parsing or use our parser that has some error checking already implemented.

### CustomResponseObject.java
This class is a helper class that wraps org.json to handle the responses we receive from an API. It takes in a String of XML, JSON, or JSON Array and converts it into a dictionary that is then accessible to the tester from get methods.
Some limitations include the fact that this still takes XML and doesn't yet verify that it is a SOAP response. Future iterations will likely change this feature so it verifies and confirms a SOAP response before parsing.

### Currently usable features
-	Testing SOAP and REST
-	Handling and parsing SOAP and REST responses via a single custom response class
-	We can set headers, cookies, get response headers, get response bodies, get response codes and code descriptions
-	We can set a payload 
-	We can perform GET, POST, PUT, DELETE, HEAD, and OPTIONS requests to a server
-	The current authentications we can handle are: OAuth, basic auth, and no authentication

### Future development (In Progress)
- Handling SOAP Faults
- Verifying SOAP messages
- Handling SSOi Authentication
