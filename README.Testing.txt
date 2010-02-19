Following synchronization cases have to be tested:

# Contacts

## Preparing the Testdata

* Delete all data:
** on the Server (via IMAP Folder)
** in the Thunderbird addressbook
** on the Testdevice (press "Reset")
** Synckolab's cache in profile\synckolab\contact\SyncTest
* Load data from SyncTest.vcf (Addressbook, Tools, Actions for contacts, Import from vCard/vcf) into Thunderbird
* Upload the test data to the Server with Synckolab (accept "copy all items to Server dialog")

* Synchronize the Phone
** Expected: KolabDroid downloads all eight Contacts to the phone.
** Check:
*** Phone has the same Contacts a Thunderbird
*** Phone has received non-ASCII characters correctly (e.g. "Charlü McÜmläüt")

* Synchronize the Phone again
** Expected: KolabDroid indicates no changes

* Synchronize Thunderbird
** Expected: Synckolab indicates no changes

## Testing synchronization

* Make the following changes to different Contacts. Data on the Server can be edited via Thunderbird and Synckolab
** New on Server: New Servör <new.server@example.com>
** New on Phone: New Phöne <new.phone@example.com>

** Edit on Server: Alice Pattern: -> alice.server@example.org
** Edit on Phone: Charlü McÜmläut: -> charlymcue.phone@example.com
** Edit the same Contact on both sides: Dorothy Dougherthy: change home number on Phone and work number on Server

** Delete on Server: Emily Emilia
** Delete on Phone: Franz Ferdinand
** Delete on both sides: Gustav Grunt

** Edit on Server, Delete on Phone: Bob Bazonga -> bob.server@example.org
** Edit on Phone, Delete on Server: Maximilian Muster -> max.phone@example.net

* Synchronize Thunderbird again
** Expected: Synckolab uploads the changes in the testdata to the server

* Synchronize Thunderbird again
** Expected: Synckolab indicates no changes

* Synchronize the Phone
** Expected: 
*** KolabDroid fetches the changes from the server and uploads the local changes to the server;
*** KolabDroid indicates a conflict on the Contact that was edited on both the Server and the Phone
** Check:
*** Changes from the Server propagated correctly to the Phone
*** Conflicted Contact (Dorothy Dougherthy) is now duplicated, once with changes from the Phone, once with changes from Server

* Synchronize the Phone again
** Expected: KolabDroid indicates no changes

* Synchronize Thunderbird
** Expected: Synckolab downloads the changes in the testdata from the server
** Check:
*** Changes from the Phone propagated correctly to the Server
*** Conflicted Contact (Dorothy Dougherthy) is now duplicated, once with local changes, once with changes from Server

* Synchronize Thunderbird again
** Expected: Synckolab indicates no changes

* Synchronize the Phone again
** Expected: KolabDroid indicates no changes

## Test safety net

* Delete all Contacts except two from the Server

* Synchronize the Phone
** Expected: 
*** KolabDroid indicates no/very few Contacts found on Server
*** KolabDroid refuses to delete local data
** Check: local Data untouched

* Synchronize the Phone again, overriding Default on refusal Dialog
** Expected: KolabDroid accepts user input and deletes (most) local Contacts
** Check: local Data wiped


