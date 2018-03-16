
package Tool;

import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {
	
	@SuppressWarnings("rawtypes")
	public boolean sendMailFilter(String from,String to,TreeSet<String> ccList, String subject, String msgContent){
		Properties props = new Properties();  

		props.put("mail.smtp.host", "midsmtp.aetna.com");  
		Session session = Session.getInstance(props);
		
		try{
			Message msg = new MimeMessage(session);  
			msg.setFrom(new InternetAddress(from));	

			InternetAddress[] address = {new InternetAddress(to)}; 

			msg.setRecipients(RecipientType.TO, address);
			Iterator iterator = ccList.iterator();
			while (iterator.hasNext()){
				msg.addRecipients(RecipientType.CC, InternetAddress.parse((String)iterator.next()));
			}
			msg.setSubject(subject);
			msg.setSentDate(new Date());  
			msg.setContent(msgContent, "text/html; charset=utf-8");
			Transport.send(msg);  
		}
		catch (MessagingException e) {
			e.printStackTrace();
		} 
		return true;
	}
}
