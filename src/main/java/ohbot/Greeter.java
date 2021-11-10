package ohbot;

/**
 * Created by lambertyang on 2017/1/13.
 */
public class Greeter {
	
	public static String sayHello() {
        return "<html>\n<head>\n<title> Piggy Test Page</title>\n</head>\n<body>\nHello World, My name is Piggy!\n</body>\n</html>\n";
    }
	
    public static String sayHelloToGoogleAuth() {
        return "<html>\n<head>\n<meta name=\"google-site-verification\" content=\"NpDCWKInZP6DC18AE31GEnC-n7jaiWXKIMIdA-ztf24\" />\n<title> Piggy Test Page</title>\n</head>\n<body>\nHello Google, My name is Piggy!\n</body>\n</html>\n";
    }
    
    public static String sayHelloToDropBoxAuth(String challenge) {
        return "<html>\n<head>\n<meta name=\"dropbox-site-verification\" challenge=\""+challenge+"\" />\n<title> Piggy Test Page</title>\n</head>\n<body>\nHello DropBox, My name is Piggy!\n</body>\n</html>\n";
    }
}
