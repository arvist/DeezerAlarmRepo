package com.cikoapps.deezeralarm.helpers;

/**
 * Created by arvis.taurenis on 3/8/2015.
 */
public class Quotes {
    private static final String[] quotes = {
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
            "The only thing that stands between you and your dream is the will to try and the belief that it is actually possible.",
            "When we love, we always strive to become better than we are. When we strive to become better than we are, everything around us becomes better too.",
            "Risks must be taken because the greatest hazard in life is to risk nothing.",
            "All our dreams can come true if we have the courage to pursue them.",
            "Great minds discuss ideas; average minds discuss events; small minds discuss people.",
            "When you stop chasing the wrong things, you give the right things a chance to catch you.",
            "The starting point of all achievement is desire.",
            "All progress takes place outside the comfort zone.",
            "We become what we think about most of the time, and that's the strangest secret.",
            "I find that when you have a real interest in life and a curious life, that sleep is not the most important thing.",
            "Don't let the fear of losing be greater than the excitement of winning.",
            "Motivation is what gets you started. Habit is what keeps you going.",
            "The individual who says it is not possible should move out of the way of those doing it.",
            "Everyone wants to live on top of the mountain, but all the happiness and growth occurs while you're climbing it."
    };
    private static final String[] authors = {
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
            "Joel Brown",
            "Paulo Coelho",
            "Leo Buscaglia",
            "Walt Disney",
            "Eleanor Roosevelt",
            "Lolly Daskal",
            "Napoleon Hill",
            "Michael John Bobak",
            "Earl Nightingale",
            "Martha Stewart",
            "Robert Kiyosaki",
            "Jim Ryun",
            "Tricia Cunningham",
            "Andy Rooney"
    };
    public final String quote;
    public final String author;

    private Quotes(String author, String quote) {
        this.author = author;
        this.quote = quote;
    }

    public static Quotes getQuote() {
        int number = HelperClass.randomInteger(0, quotes.length);
        return new Quotes(authors[number], quotes[number]);
    }

}
