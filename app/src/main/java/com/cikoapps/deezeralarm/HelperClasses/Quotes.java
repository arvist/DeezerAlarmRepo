package com.cikoapps.deezeralarm.HelperClasses;

import java.util.Random;

/**
 * Created by arvis.taurenis on 3/8/2015.
 */
public class Quotes {
    public String quote;
    public String author;

    private static String[] quotes = {
            "Life isn’t about getting and having, it’s about giving and being.",
            "We can do anything we want to do if we stick to it long enough.",
            "Your time is limited, so don’t waste it living someone else’s life.",
            "Whether you think you can or you think you can’t, you’re right.",
            "Life is not always perfect. Like a road, it has many bends, ups and down, but that’s its beauty.",
            "Life will just not wait for us to live it. We are in it, now, and Now is the time to Live.",
            "Be not afraid of life. Believe that life is worth living, and your belief will help create the fact.",
            "When I hear somebody sigh, ‘Life is hard,’ I am always tempted to ask, ‘Compared to what?",
            "The real opportunity for success lies within the person and not in the job.",
            "Accept responsibility for your life. Know that it is you who will get you where you want to go, no one else.",
            "Challenges are what make life interesting and overcoming them is what makes life meaningful.",
            "Happiness cannot be traveled to, owned, earned, or worn. It is the spiritual experience of living every minute with love, grace & gratitude.",
            "In order to succeed, your desire for success should be greater than your fear of failure.",
            "Don’t worry about failures, worry about the chances you miss when you don’t even try.",
            "The only thing that stands between you and your dream is the will to try and the belief that it is actually possible."};
    private static String[] authors = {
            "Kevin Kruse",
            "Helen Keller",
            "Steve Jobs",
            "Henry Ford",
            "Amit Ray",
            "Michelle Geaney",
            "William James",
            "Sydney Harris",
            "Zig Ziglar",
            "Les Brown",
            "Joshua J. Marine",
            "Denis Waitley",
            "Bill Cosby",
            "Jack Canfield",
            "Joel Brown"};

    public Quotes(String author, String quote) {
        this.author = author;
        this.quote = quote;
    }

    public static Quotes getQuote() {
        int number = randInt(0, quotes.length);
        return new Quotes(authors[number], quotes[number]);
    }

    ;

    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min)) + min;

        return randomNum;
    }
}
