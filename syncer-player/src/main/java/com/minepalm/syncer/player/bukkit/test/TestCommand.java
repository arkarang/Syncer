package com.minepalm.syncer.player.bukkit.test;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class TestCommand implements CommandExecutor {

    private final LoopTest test;
    private final DuplicateFinder finder;

    @Override
    @SneakyThrows
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.isOp()){
            if(strings.length >= 1){
                switch (strings[0]){
                    case "test1":
                        test.test1(Integer.parseInt(strings[1]));
                        return true;

                    case "dupecheck":
                        finder.execute();
                        return true;
                    case "dupecheck2":
                        finder.execute2();
                        return true;
                }
            }
        }
        return false;
    }

}