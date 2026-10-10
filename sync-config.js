/* ---------------------------------------------------------------------------
   Screens (phone + TV) — optional setup.

   To let your phone control the timer on a TV / Fire TV stick, paste the address
   of your free Firebase Realtime Database between the quotes below, e.g.

     databaseURL: 'https://my-gym-timer-default-rtdb.europe-west1.firebasedatabase.app'

   Leave it empty and the app still works normally on one device; the TV/phone
   pairing then only works between tabs of the same browser (a demo).
   --------------------------------------------------------------------------- */
window.SYNC_CONFIG = {
  databaseURL: 'https://interval-timer-4f6b0-default-rtdb.europe-west1.firebasedatabase.app/'
};
