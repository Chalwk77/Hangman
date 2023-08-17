/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import static com.chalwk.game.Globals.concurrentGames;
import static com.chalwk.game.Globals.guesses;

public class OnMessage {
    public static void onMessage(MessageReceivedEvent event) {

        Member member = event.getMember();
        String memberID = member.getId();

        for (Game game : concurrentGames) {
            if (game == null) continue;

            String challengerID = game.challengerID;
            String opponentID = game.opponentID;

            if (memberID.equals(challengerID) || memberID.equals(opponentID) && game.started) {

                String input = event.getMessage().getContentRaw();
                String word = game.word;
                //
                // Player is attempting to guess the whole word:
                //
                if (input.length() > 1) {
                    if (input.contentEquals(word)) {
                        // you guessed the word / game over
                    } else {
                        // you win
                    }
                    //
                    // Player is attempting to guess a letter:
                    //
                } else if (!getGuesses(input, new StringBuilder(word), guesses)) {
                    // you guessed wrong
                }

                if (guesses.size() == word.length()) {
                    // you lose
                }

                game.setStage(game.state--);
                updateEmbed(new StringBuilder(word), guesses, game, event);
            }
        }
    }

    private static void updateEmbed(StringBuilder word, List<Character> guesses, Game game, MessageReceivedEvent event) {
        event.getMessage().delete().queue(); // delete the player's message input
        EmbedBuilder embed = game.getEmbed();
        String messageID = game.getEmbedID();

        embed.addField("Characters:", "```" + guessBox(word, guesses) + "```", false);

        event.getChannel()
                .retrieveMessageById(messageID)
                .queue(message -> message.editMessageEmbeds(embed.build()).queue());
    }

    private static String guessBox(StringBuilder word, List<Character> guesses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char guess = word.charAt(i);
            if (guesses.contains(guess)) {
                sb.append("〔").append(guess).append("〕");
            } else {
                sb.append("〔 〕");
            }
        }
        return sb.toString();
    }

    private static boolean getGuesses(String input, StringBuilder word, List<Character> guesses) {
        char guess = input.charAt(0);
        guesses.add(guess);
        return word.toString().contains(input);
    }
}