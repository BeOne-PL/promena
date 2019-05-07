import groovy.io.FileType

new File(new File(request.outputDirectory), request.artifactId).eachFileRecurse(FileType.FILES) {
    if(it.name == "pom.xml") {
        removeToDeleteParent(it)
    }
}

def removeToDeleteParent(File pomFile) {
    def pomFileText = pomFile.text

    if(pomFileText.contains("to-delete")) {
        def pomWithoutParent = pomFileText -
                ~/.*<parent>.*/ -
                ~/.*<groupId>to-delete<\/groupId>.*/ -
                ~/.*<artifactId>to-delete<\/artifactId>.*/ -
                ~/.*<version>to-delete<\/version>.*/ -
                ~/.*<\/parent>.*/ -
                ~/\n\n\n\n\n\n/

        pomFile.write(pomWithoutParent)
    }
}