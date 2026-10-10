/*
  FROGUE'S EXTRA MESSAGES
  ------------------------------------------------------------------
  Add your own lines for Frogue's speech bubble here. Whatever you add is
  mixed in at random with the built-in messages, so every now and then you'll
  get one of yours. Nothing here is required: delete a line, leave a list
  empty ([]), or delete this whole file and the timer still works.

  Quick rules
    - Each list is a set of quoted lines separated by commas.
    - Keep lines short (a few words fit best on a phone).
    - Use straight quotes "like this". If a line contains a quote or
      apostrophe, wrap it in the other kind of quote: "Haz's going to love this".
    - You can drop in these placeholders:
        {exercise}   the exercise name that is starting or coming up first
        {next}       what comes after a rest
        {round}      the round just finished / current round number
        {nextRound}  the number of the round about to start
        {rounds}     total number of rounds
    - After editing, refresh the page (on an installed copy, close and
      reopen the app once or twice so it picks up the new file).

  Set replace to true if you want ONLY your lines for a list that has some
  (the built-in ones for that list are then skipped). Leave it false to mix.
*/
window.FROGUE_MESSAGES = {
  replace: false,

  // while you are working
  work: [
    // "Haz would be proud!",
    // "Squeeze it out!",
  ],

  // during an ordinary rest (use {next} for what's coming)
  rest: [
    // "Shake the legs out, then {next}!",
  ],

  // the first moments of the get-ready countdown
  prep: [
    // "Here we go. First up: {exercise}!",
  ],

  // the first couple of seconds of a round break
  roundBreak: [
    // "Round {round} done. Absolute legend!",
  ],

  // the rest of a round break
  roundBreakSettled: [
    // "Sip, breathe, round {nextRound} is coming.",
  ],

  // the little speech bubble on the "Round complete" screen
  roundComplete: [
    // "Round {round} in the bag!",
  ],

  // the final exercise of the whole workout
  lastInterval: [
    // "Last one. Empty the tank!",
  ],

  // the first exercise of the final round
  lastRound: [
    // "Final round. Make it count!",
  ],

  // once, about halfway through
  halfway: [
    // "Halfway there. Keep going!",
  ],

  // when you pause
  paused: [
    // "Take your time. I'll wait.",
  ],

  // when you come back after a refresh
  welcomeBack: [
    // "You're back! Let's finish this.",
  ],

  // the finish screen
  finish: [
    // "Frogue says: you smashed it!",
  ],
};
