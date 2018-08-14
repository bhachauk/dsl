import javax.mail.internet.*
import javax.mail.*

/**
 * This class has methods for sending Emails
 * <h4>Example 1: Simple Mail from nmsworks.co.in </h4>
 * <pre>
 *     mailserver{
 *             sethost '192.168.9.44'
 *
 *             login 'bhanuchanderu@nmsworks.co.in'
 *
 *             sendmsg{
 *
 *                    to 'example@gmail.com'
 *                    cc 'any@gmail.com'
 *                    bcc 'any@gmail.com'
 *                    subject 'DSL-EMAIL UTIL'
 *                    body'''
 *                    ....content ..
 *                    '''
 *                    attach 'dsl.groovy'
 *
 *             }
 *
 *      }
 * </pre>
 * <h4>Example 2: HTML Mail </h4>
 * <pre>
 *     mailserver{
 *             sethost 'smtp.gmail.com' // put_your_SMTP_host_name
 *             login 'bhanuchander210@gmail.com','your_password'
 *
 *             sendmsg{
 *
 *                    to 'example@gmail.com'
 *                    cc 'any@gmail.com'
 *                    bcc 'any@gmail.com'
 *                    subject 'DSL-EMAIL UTIL'
 *                    body'''
 *                    ....content ..
 *                    '''
 *                    attach 'dsl.groovy'
 *
 *             }
 *
 *      }
 * </pre>
 *  * <h4>Example 3: Determine content type </h4>
 * <pre>
 *     mailserver{
 *             sethost 'smtp.gmail.com' // put_your_SMTP_host_name
 *             login 'bhanuchander210@gmail.com','your_password'
 *
 *             sendmsg{
 *
 *                    to 'example@gmail.com'
 *                    cc 'any@gmail.com'
 *                    bcc 'any@gmail.com'
 *                    subject 'DSL-EMAIL UTIL'
 *                    body'''
 *                    ....Plain_Text_content ..
 *                    '''
 *                    body 'text/html','''       // Add the type of content
 *                    <pre>html content</pre>
 *                    ```
 *                    attach 'dsl.groovy'
 *
 *             }
 *
 *      }
 * </pre>
 */

Email mailserver(Closure cl)
{
    def email = new Email()
    cl.delegate = email
    cl()
    return email
}

class Email {

    String from
    String password
    String host
    boolean auth_status
    MimeMessage message
    Transport transport
    Integer port_no=25

    void login(String fromname, String pswd=null)
    {
        from=fromname
        password=pswd
    }

    void sethost(String hostname)
    {
        host=hostname
    }
    void setport(Integer portno)
    {
        port_no=portno
    }

    void init()
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable",true);
        props.setProperty("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.host", host);

        if(password!=null){

            props.put("mail.smtp.user", from);
            props.put("mail.smtp.password", password);
            auth_status=true
        }

        props.put("mail.smtp.auth",auth_status);
        props.put("mail.smtp.port", port_no);//587

        Session session = Session.getInstance(props, null);
        message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));

        transport = session.getTransport("smtp");
        transport.connect(host, from, password);
    }

    SendMail sendmsg(Closure cls)
    {
        init()
        SendMail p = new SendMail()
        cls.delegate = p
        cls.resolveStrategy = Closure.DELEGATE_FIRST
        cls.call()
        p.send()
        return p
    }

    class SendMail
    {

        Multipart multipart = new MimeMultipart()

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

        void body(String type="text/plain",String body)

        {
            MimeBodyPart messageBodyPart = new MimeBodyPart()

            messageBodyPart.setContent(body,type)

            multipart.addBodyPart(messageBodyPart)
        }

        void attach (String...filename)

        {

            for(String each:filename){

                MimeBodyPart messageBodyPart = new MimeBodyPart()

                messageBodyPart.attachFile(each)

                multipart.addBodyPart(messageBodyPart)

                println "File Name : '"+each+ "' successfully attached"

            }

        }

        void send()

        {
            message.setContent(multipart)

            transport.sendMessage(message, message.getAllRecipients())

            transport.close()

            println "Message Successfully Sent"
        }

    }

}