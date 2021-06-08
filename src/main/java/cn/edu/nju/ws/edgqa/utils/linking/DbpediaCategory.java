package cn.edu.nju.ws.edgqa.utils.linking;

import java.io.Serializable;

public class DbpediaCategory implements Serializable {
    private String uri;
    private String label;
    private String localName;

    public DbpediaCategory(String uri, String label, String localName) {
        this.uri = uri;
        this.label = label;
        this.localName = localName;
        removeAngleBracket();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void removeAngleBracket() {
        int begin = 0;
        int end = uri.length();
        if (uri.startsWith("<"))
            begin += 1;
        if (uri.endsWith(">"))
            end -= 1;
        uri = uri.substring(begin, end);
    }
}
