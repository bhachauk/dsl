mailserver{
    sethost '192.168.9.44'

    login 'bhanuchanderu@nmsworks.co.in'

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