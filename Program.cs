using System;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Office.Interop.Outlook;
using System.Globalization;
using System.Timers;
using System.Threading;

namespace EmailSearcher {
    class Program {
        static void Main(string[] args) {
            while (true) {
                Console.WriteLine("*** calling SearchEMail *** ");
                searchEmail();
                Thread.Sleep(60 * 15 * 1000);
            }
        }

        private static void searchEmail() {
            Application outlookApplication = null;
            NameSpace outlookNamespace = null;
            MAPIFolder inboxFolder = null;
            Items mailItems = null;

            try {
                int count = 0;
                int iterationCount = 0;
                outlookApplication = new Application();
                outlookNamespace = outlookApplication.GetNamespace("MAPI");
                inboxFolder = outlookNamespace.GetDefaultFolder(OlDefaultFolders.olFolderInbox);
                mailItems = inboxFolder.Items;
                DateTime mailDate;

                foreach (Object objItem in mailItems) {
                    iterationCount = iterationCount + 1;
                    if (objItem is Microsoft.Office.Interop.Outlook.MailItem) {
                        Microsoft.Office.Interop.Outlook.MailItem item = (Microsoft.Office.Interop.Outlook.MailItem)objItem;
                        {
                            mailDate = item.ReceivedTime;
                            if (mailDate.Date == searchMailConfig.todaysDate.Date) {
                                if (item.UnRead == true) {
                                    if (item.SenderEmailAddress.Equals(searchMailConfig.from, StringComparison.InvariantCultureIgnoreCase)) {
                                        Console.WriteLine(item.Subject.ToUpper() + "-->" + searchMailConfig.demandMailSub.ToUpper());
                                        if (item.Subject.ToUpper().Contains(searchMailConfig.demandMailSub.ToUpper())) {
                                            Console.WriteLine("Demand Mail Found");
                                            item.UnRead = false;
                                            for (int i = 1; i <= item.Attachments.Count; i++) {
                                                Console.WriteLine("Attachment: " + item.Attachments[i].FileName);
                                                if (searchMailConfig.extensionsArray.Any(item.Attachments[i].FileName.Contains)) {
                                                    // the filename contains one of the extensions
                                                    Console.WriteLine(item.Attachments[i].FileName);
                                                    count = count + 1;
                                                    item.Attachments[i].SaveAsFile(searchMailConfig.pathToSaveFile + item.Attachments[i].FileName);
                                                }
                                            }
                                        } else if (item.Subject.ToUpper().Contains(searchMailConfig.talentMailSub.ToUpper())) {
                                            Console.WriteLine("Talent Mail Found");
                                            item.UnRead = false;
                                            for (int i = 1; i <= item.Attachments.Count; i++) {
                                                Console.WriteLine("Attachment: " + item.Attachments[i].FileName);
                                                if (searchMailConfig.extensionsArray.Any(item.Attachments[i].FileName.Contains)) {
                                                    // the filename contains one of the extensions
                                                    Console.WriteLine(item.Attachments[i].FileName);
                                                    count = count + 1;
                                                    item.Attachments[i].SaveAsFile(searchMailConfig.pathToSaveFile + item.Attachments[i].FileName);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (count == 2) {
                        System.Diagnostics.Process.Start(searchMailConfig.pathToSaveFile + "Tools\\DemandTool.jar");
                        break;
                    } else if (iterationCount >20) {
                        Console.WriteLine("*** Stopped...Checked 20 mails...Releasing Resources...Waiting for next Call *** ");
                        ReleaseComObject(mailItems);
                        ReleaseComObject(inboxFolder);
                        ReleaseComObject(outlookNamespace);
                        ReleaseComObject(outlookApplication);
                        break;
                    }
                }
            } catch (System.Exception e) {
                Console.WriteLine(e.Message);
            } finally {
                Console.WriteLine("*** Stopped...Releasing Resources...Waiting for next Call *** ");
                ReleaseComObject(mailItems);
                ReleaseComObject(inboxFolder);
                ReleaseComObject(outlookNamespace);
                ReleaseComObject(outlookApplication);
            }
        }

        private static void ReleaseComObject(object obj) {
            if (obj != null) {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(obj);
                obj = null;
            }
        }
    }
}
