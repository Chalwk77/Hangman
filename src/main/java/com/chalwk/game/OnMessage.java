/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import static com.chalwk.Main.games;
import static com.chalwk.Main.removeGame;

public class OnMessage {
    public static void onMessage(MessageReceivedEvent event) {

        Member member = event.getMember();
        if (member == null) return;
        String memberID = member.getId();

        for (Game game : games) {

            String challengerID = game.challengerID;
            String opponentID = game.opponentID;

            if (memberID.equals(challengerID) || memberID.equals(opponentID) && game.started) {

                if (!yourTurn(event, game, member)) return;

                int color = 0x00ff00; // green

                String input = event.getMessage().getContentRaw();
                String word = game.word;

                if (input.length() > 1) {
                    if (input.contentEquals(word)) { // guessed the whole word
                        game.guessed_whole_word = true;
                    } else {
                        game.state--;
                    }
                } else if (!getGuess(input, new StringBuilder(word), game)) {
                    game.state--;
                    color = 0xff0000; // red
                }

                game.setStage(game.state);
                updateEmbed(new StringBuilder(word), game, event, color);
            }
        }
    }

    private static void updateEmbed(StringBuilder word, Game game, MessageReceivedEvent event, int color) {

        game.setTurn();
        event.getMessage().delete().queue();
        String guess_box = guessBox(word, game);

        EmbedBuilder embed = game.getEmbed();
        if (gameOver(word, game, event, embed)) {
            games = removeGame(games, game);
            return;
        }

        embed.setDescription("It's now " + game.whos_turn + "'s turn.");
        embed.addField("Characters:", guess_box, false);
        embed.addField("Guesses: " + showGuesses(game.guesses), " ", false);
        embed.setColor(color);
        editEmbed(game, event, embed);
    }

    private static boolean yourTurn(MessageReceivedEvent event, Game game, Member member) {
        if (!member.getEffectiveName().equals(game.whos_turn)) {
            event.getMessage().delete().queue();
            return false;
        }
        return true;
    }

    private static boolean gameOver(StringBuilder word, Game game, MessageReceivedEvent event, EmbedBuilder embed) {
        if (word.length() == game.correct || game.guessed_whole_word) {
            embed.addField("\uD83D\uDFE2 GAME OVER. The word was (" + word + "). " + game.whos_turn + " wins!", " ", false);
            embed.setColor(0x00ff00);
            editEmbed(game, event, embed);
            return true;
        } else if (game.state == 0) {
            embed.addField("\uD83D\uDD34 GAME OVER. The word was (" + word + "). The man was hung!", " ", false);
            embed.setColor(0xff0000);
            editEmbed(game, event, embed);
            return true;
        }
        return false;
    }

    private static String showGuesses(List<Character> guesses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < guesses.size(); i++) {
            sb.append(guesses.get(i).toString().toUpperCase());
            if (i != guesses.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static void editEmbed(Game game, MessageReceivedEvent event, EmbedBuilder embed) {
        event.getChannel()
                .retrieveMessageById(game.getEmbedID())
                .queue(message -> message.editMessageEmbeds(embed.build()).queue());
    }

    private static String guessBox(StringBuilder word, Game game) {
        StringBuilder sb = new StringBuilder();
        sb.append("```");
        game.correct = 0;
        for (int i = 0; i < word.length(); i++) {
            char guess = word.charAt(i);
            if (game.guesses.contains(guess)) {
                game.correct++;
                sb.append("〔").append(guess).append("〕");
            } else {
                sb.append("〔 〕");
            }
        }
        sb.append("```");
        return sb.toString();
    }


    private static boolean getGuess(String character, StringBuilder word, Game game) {
        char guess = character.charAt(0);
        game.guesses.add(guess);
        return word.toString().contains(character);
    }
}
