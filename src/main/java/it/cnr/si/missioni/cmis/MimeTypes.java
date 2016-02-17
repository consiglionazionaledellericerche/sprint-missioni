package it.cnr.si.missioni.cmis;

public enum MimeTypes
{
    HTML ("text/html"),
    XHTML ("text/xhtml"),
    TEXT ("text/plain"),
    JAVASCRIPT ("text/javascript"),
    XML ("text/xml"),
    PDF ("application/pdf"),
    ATOM ("application/atom+xml"),
    ATOMFEED ("application/atom+xml;type=feed"),
    ATOMENTRY ("application/atom+xml;type=entry"),
    FORMDATA ("multipart/form-data"),
    JSON ("application/json");
    
    private String mimetype;

    MimeTypes(String mimetype)
    {
        this.mimetype = mimetype;
    }
    
    public String mimetype()
    {
        return mimetype;
    }
}