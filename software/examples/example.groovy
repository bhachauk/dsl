import io.github.bhanuchander210.filestudy.EmailDSL
EmailDSL.mailserver{
    sethost 'smtp.gmail.com'
    login 'bhanuchander210@gmail.com','*******'

    sendmsg{

        to 'example@gmail.com'
        cc 'any@gmail.com'
        bcc 'any@gmail.com'
        subject 'DSL-EMAIL UTIL'
        body'''
                    ....content ..
            '''
        attach 'dsl.groovy'

    }

}
