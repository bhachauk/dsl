String fileName = '/home/bhanuchander/test/txt/temp.txt'

println new File(fileName).text

filestudy {

    inputFile fileName

    filterLine 'Alarm_Object'

    result{

        println line
        println getBetweenString ('Alarm_Object = ')
        println getBetween ('Alarm_Object = "',1,'"',2)
    }
}


/*Output:
OSI Alarm : Resynchronization ...
Alarm_Object = "11"
Managed_Object = 'EqHolder'
Event_Type = "CommunicationsAlarm"
Event_Time = "200"
Probable_Cause = "me"
Perceived_Severity = "Major"
Additional_Text = BGL_TNGKA_q1|unwanted..something...


OSI Alarm : Resynchronization ...
Alarm_Object = "19"
Managed_Object = 'Equipment'
Event_Type = "CommAlarm"
Event_Time = "200"
Probable_Cause = "you"
Perceived_Severity = "Minor"
Additional_Text = BGL_TNGKA_YMC_A_T0837|unwanted..something...

-----------------------------
Alarm_Object = "11"
"11"
11
Alarm_Object = "19"
"19"
19
*/