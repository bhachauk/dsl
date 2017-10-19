import javax.mail.internet.*
import javax.mail.*
import javax.activation.*

def email(Closure cl) {

    def email = new Email()
    cl.delegate = email
    cl()
    return email
}

 class Email {

    MimeMessage message
    Session session
    String host = "smtp.gmail.com";//"fileserver.nmsworks.co.in"
    Properties props = System.getProperties();
    Transport transport
    String from
    String password
    BodyPart messageBodyPart
    Multipart multipart
    DataSource source


    void login (String from, String password)
    
    {

    props.put("mail.smtp.starttls.enable",true);
    /* mail.smtp.ssl.trust is needed in script to avoid error "Could not convert socket to TLS"  */
    props.setProperty("mail.smtp.ssl.trust", host);
    props.put("mail.smtp.auth", true);      
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.user", from);
    props.put("mail.smtp.password", password);
    props.put("mail.smtp.port", "25");//587
 
    session = Session.getDefaultInstance(props, null);
    message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));

    transport = session.getTransport("smtp");
    transport.connect(host, from, password);

    }

    void construct(def to=[],String type)
    {
        for(String each:to)
        {

            InternetAddress toAddress = new InternetAddress(each)
            message.addRecipient(Message.RecipientType."$type", toAddress)
    
        }
    }

    void to (String... to)
    { 
        construct(to,'TO')
    }

    void cc (String... to)
    {
        construct(to,'CC')
    }

    void bcc (String...to)
    {
        construct(to,'BCC')
    }

    void subject (String subject) 
    { 
        message.setSubject(subject)
    }

    void body(String body) 
    {   	
        message.setText(body)
    }

    void attach (String...filename)

    {
        messageBodyPart = new MimeBodyPart()
        
        messageBodyPart.setText("Please Find the attachment file here")

        multipart = new MimeMultipart()

        multipart.addBodyPart(messageBodyPart)

        messageBodyPart = new MimeBodyPart()

        for(String each:filename){

            source = new FileDataSource(each)

            messageBodyPart.setDataHandler(new DataHandler(source))

            messageBodyPart.setFileName(each)

            multipart.addBodyPart(messageBodyPart)

            println ("File Name : "+each+ "successfully attached")

        }

        message.setContent(multipart)

    }

    void send()
    {

        transport.sendMessage(message, message.getAllRecipients())
        
        transport.close()
        
        println ("Message Successfully Sent")
    }

}