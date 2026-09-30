#include <jni.h>
#include <string>
#include<sys/socket.h>
#include<netinet/in.h>
#include<unistd.h>
extern "C" JNIEXPORT jstring JNICALL Java_com_example_apktest_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT jint JNICALL Java_com_example_apktest_MainActivity_addNumbers(
        JNIEnv* env,jobject,jint a, jint b
        ){
    int sum = a+b;
    return sum;
}




extern "C" JNIEXPORT jstring JNICALL Java_com_example_apktest_MainActivity_startServer(
        JNIEnv* env,jobject
){
    int server_fd = socket(AF_INET,SOCK_STREAM,0);

    if (server_fd == -1) {
        char error_message[256];
        snprintf(error_message, sizeof(error_message), "Socket failed: %s", strerror(errno));
        return env->NewStringUTF(error_message);
    }
    sockaddr_in address{};
    address.sin_family = AF_INET;
    address.sin_addr.s_addr= INADDR_ANY;
    address.sin_port = htons(8080);
    if(bind(server_fd,(struct  sockaddr*)&address,sizeof (address))<0){
        close(server_fd);
        return env->NewStringUTF("Bind failed at port 8080");
    }
    listen(server_fd,3);
    return env->NewStringUTF("C++ socket server listening on port 8080...");

    sockaddr_in client_addr{};
    socklen_t  client_len = sizeof(client_addr);

    int client_fd = accept(server_fd,(struct sockaddr*)&client_addr,&client_len);

    if(client_fd<0){
        close(server_fd);
        return  env ->NewStringUTF("Accept failed");
    }

    char buffer[1024] ={0};

    ssize_t bytes_recieved = recv(client_fd,buffer,sizeof(buffer)-1,0);

    std::string recieved_msg ="No data";
    if(bytes_recieved>0){
        recieved_msg = std::string(buffer,bytes_recieved);
    }
    const char* reply = "Hello from android c++ hyyyyy";
    send(client_fd,reply,strlen(reply),0);

    close(client_fd);
    close(server_fd);
std::string status ="recieved: "+ recieved_msg;
return env ->NewStringUTF(status.c_str());
}



