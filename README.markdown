Outlook <-> JIRA Connector
==========================

As name says: this is java application that provides integration between
Micro$oft Office Outlook and Atlassian JIRA

__Features:__

- Get appointments from Outlook and log work against matched JIRA issues

__Requirements:__

Currently I'm testing against Outlook 2007/2010 for Windows, Outlook 2011 for Mac OS and JIRA 4.4

__Release Notes:__

_1.1_
- Initial windows support
_1.2_
- Mac OS Outlook integration!!!

__Known Issues:__

Monthly and Yearly recurring events are not supported under Mac OS.

__Usage:__

Date format: dd.mm.yyyy

_Windows:_
outlook-connector.exe date-from date-to

_Mac OS:_
sh pump.sh date-from date-to


_Example:_

Log all events from 6-th February 2012 to 13-th February 2012
outlook-connector.exe 06.02.2012 13.02.2012

