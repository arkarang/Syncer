package com.minepalm.syncer.player.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.Pair;
import com.minepalm.syncer.player.bukkit.gui.PlayerDataGUIFactory;
import com.minepalm.syncer.player.mysql.MySQLPlayerEnderChestDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerLogDatabase;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@CommandPermission("rendog.admin")
@CommandAlias("invadmin|inva")
public class InspectCommands extends BaseCommand {

    private final BukkitExecutor executor;
    private final MySQLPlayerLogDatabase logDatabase;

    private final MySQLPlayerInventoryDataModel inventoryDatabase;
    private final MySQLPlayerEnderChestDataModel enderChestModel;
    private final PlayerDataGUIFactory factory;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/hh:mm");

    private final Map<UUID, List<Pair<PlayerDataLog, String>>> map = new ConcurrentHashMap<>();

    private final SimpleDateFormat logFormat = new SimpleDateFormat("yyyy/MM/dd/hh:mm:ss");

    @Subcommand("search")
    public void inspect(Player player, String username, String range, @Default("none") String rangeMax) {

        executor.async(() -> {
            try {
                Date date = format.parse(range);
                OfflinePlayer off = Bukkit.getOfflinePlayer(username);
                if (off == null) {
                    player.sendMessage("해당 플레이어는 존재하지 않습니다. ");
                    return;
                }
                if (rangeMax.equals("none")) {
                    List<Pair<PlayerDataLog, String>> list = logDatabase.select(off.getUniqueId(), date.toInstant().toEpochMilli()).get();
                    map.put(player.getUniqueId(), list);
                    cacheAndPrint(player, list);
                } else {
                    Date dateMax = format.parse(rangeMax);
                    long min = date.toInstant().toEpochMilli();
                    long max = dateMax.toInstant().toEpochMilli();
                    List<Pair<PlayerDataLog, String>> list = logDatabase.select(off.getUniqueId(), min, max).get();
                    cacheAndPrint(player, list);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                player.sendMessage("올바르지 않은 시간 양식입니다. yyyy/MM/dd/hh:mm  예) 2022/07/25/01:30");
            }
        });

    }
    @Subcommand("ender")
    public void openEnder(Player player, int index){
        if(!map.containsKey(player.getUniqueId())){
            player.sendMessage("검색을 하지 않았습니다. 검색 하고 나서 사용해주세요.");
            return;
        }
        List<Pair<PlayerDataLog, String>> log = map.get(player.getUniqueId());

        if(index < 0){
            player.sendMessage("0 이상의 숫자를 입력해주세요.");
            return;
        }
        if(index >= log.size()){
            player.sendMessage(log.size()+" 미만의 이상의 숫자를 입력해주세요.");
            return;
        }

        executor.async(()->{
            try{
                val gui = factory.buildEnderChest(log.get(index).getKey().uuid, log.get(index).getKey());
                executor.sync(()-> gui.openGUI(player));
            }catch (IOException e){
                e.printStackTrace();
            }
        });

    }

    private void cacheAndPrint(Player player, List<Pair<PlayerDataLog, String>> list){
        map.put(player.getUniqueId(), list);
        int count = 0;
        for (Pair<PlayerDataLog, String> log : list) {
            Component chat = print(count, log.getKey(), log.getValue());
            player.sendMessage(chat);
            count++;
        }
    }

    private Component print(int count, PlayerDataLog log, String desc){
        return Component.empty()
                .append(Component.text(count+". "+log.getTask_name()
                        +" | "+logFormat.format(log.getLog_generated_time())
                        +" | "+logFormat.format(log.getData_generated_time())+ " | "+desc))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/inva open "+count))
                .hoverEvent(HoverEvent.showText(Component.text("§f클릭시 당시 인벤토리를 봅니다.")));
    }

    @Subcommand("filter")
    public void filter(Player player, String username, String type, String range, @Default("none") String rangeMax){
        executor.async(() -> {
            try {
                Date date = format.parse(range);
                OfflinePlayer off = Bukkit.getOfflinePlayer(username);
                if (off == null) {
                    player.sendMessage("해당 플레이어는 존재하지 않습니다.");
                    return;
                }
                if (rangeMax.equals("none")) {
                    List<Pair<PlayerDataLog, String>> list = logDatabase.selectType(off.getUniqueId(), type, date.toInstant().toEpochMilli(), System.currentTimeMillis()).get();
                    map.put(player.getUniqueId(), list);
                    cacheAndPrint(player, list);
                } else {
                    Date dateMax = format.parse(rangeMax);
                    long min = date.toInstant().toEpochMilli();
                    long max = dateMax.toInstant().toEpochMilli();
                    List<Pair<PlayerDataLog, String>> list = logDatabase.selectType(off.getUniqueId(), type, min, max).get();
                    cacheAndPrint(player, list);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                player.sendMessage("올바르지 않은 시간 양식입니다. yyyy/MM/dd/hh:mm  예) 2022/07/25/01:30");
            }
        });
    }

    @Subcommand("open")
    public void open(Player player, int index){
        if(!map.containsKey(player.getUniqueId())){
            player.sendMessage("검색을 하지 않았습니다. 검색 하고 나서 사용해주세요.");
            return;
        }
        List<Pair<PlayerDataLog, String>> log = map.get(player.getUniqueId());

        if(index < 0){
            player.sendMessage("0 이상의 숫자를 입력해주세요.");
            return;
        }
        if(index >= log.size()){
            player.sendMessage(log.size()+" 미만의 이상의 숫자를 입력해주세요.");
            return;
        }

        executor.sync(()->{
            try{
                factory.build(log.get(index).getKey().uuid, log.get(index).getKey()).openGUI(player);
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    @Subcommand("range")
    public void page(Player player, int rangeMin, int rangeMax){
        if(!map.containsKey(player.getUniqueId())){
            player.sendMessage("검색을 하지 않았습니다. 검색 하고 나서 사용해주세요.");
            return;
        }

        List<Pair<PlayerDataLog, String>> list = map.get(player.getUniqueId());
        int max = Math.max(rangeMax, rangeMin);
        int min = Math.min(rangeMax, rangeMin);

        if(min < 0){
            player.sendMessage("0 이상의 숫자를 입력해주세요.");
            return;
        }
        if(max >= list.size()){
            player.sendMessage(list.size()+" 미만의 이상의 숫자를 입력해주세요.");
            return;
        }

        for(int i = min ; i < max && i < list.size() ; i++){
            player.sendMessage(print(i, list.get(i).getKey(), list.get(i).getValue()));
        }
    }

    @Subcommand("modify")
    public void modify(Player player, String username){
        OfflinePlayer off = Bukkit.getOfflinePlayer(username);
        if (off == null) {
            player.sendMessage("해당 플레이어는 존재하지 않습니다.");
            return;
        }
        inventoryDatabase.load(off.getUniqueId()).thenAccept(data -> {
            executor.sync(()->{
                factory.modifyGUI(off.getUniqueId(), data).openGUI(player);
            });
        });
    }

    @Subcommand("modifyender")
    public void modifyEnder(Player player, String username){
        OfflinePlayer off = Bukkit.getOfflinePlayer(username);
        if (off == null) {
            player.sendMessage("해당 플레이어는 존재하지 않습니다.");
            return;
        }
        enderChestModel.load(off.getUniqueId()).thenAccept(data -> {
            executor.sync(()->{
                factory.modifyEnderGUI(off.getUniqueId(), data).openGUI(player);
            });
        });
    }

    @Subcommand("see")
    public void see(Player player, String username){
        OfflinePlayer off = Bukkit.getOfflinePlayer(username);
        if (off == null) {
            player.sendMessage("해당 플레이어는 존재하지 않습니다.");
            return;
        }
        executor.async(()->{
            try {
                factory.build(off.getUniqueId(), inventoryDatabase.load(off.getUniqueId()).get()).openGUI(player);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Subcommand("merge")
    public void merge(Player player){
        if(!map.containsKey(player.getUniqueId())){
            player.sendMessage("검색을 하지 않았습니다. 검색 하고 나서 사용해주세요.");
            return;
        }
        List<Pair<PlayerDataLog, String>> list = map.get(player.getUniqueId());
        executor.async(()->{
            List<PlayerDataLog> merged = new ArrayList<>();
            PlayerDataLog log = null;
            for(int i = 0; i < list.size(); i++){
                if(log == null) {
                    log = list.get(i).getKey();
                    merged.add(log);
                    continue;
                }
                PlayerDataLog target = list.get(i).getKey();
                if(!log.inventoryData.equals(target.inventoryData)){
                    merged.add(target);
                    log = target;
                }
            }
            map.put(player.getUniqueId(), merged.stream().map(pair -> new Pair<>(pair, "")).collect(Collectors.toList()));
            player.sendMessage("중복된 인벤토리를 줄여서 "+merged.size()+"개로 압축했습니다.");
        });

    }


    @Default
    public void help(Player player){
        player.sendMessage("[PlayerSyncer]");
        player.sendMessage("");
        player.sendMessage(" 날짜 입력하는법: yyyy/MM/dd/hh:mm  예) 2022/07/25/01:30");
        player.sendMessage(" /inva search <닉네임> <기준일> - 기준일로부터 지금까지 해당 유저의 기록을 검색합니다.");
        player.sendMessage(" /inva search <닉네임> <기준일> <범위최대날짜> - 기준일로부터 범위최대날짜까지 해당 유저의 기록을 검색합니다.");
        player.sendMessage(" /inva range <최소인덱스> <최대인덱스> - 검색된 인덱스에서 해당 인덱스부터 인덱스 번호까지 다시 표시해줍니다.");
        player.sendMessage(" /inva range <최소인덱스> <최대인덱스> - 검색된 인덱스에서 해당 인덱스부터 인덱스 번호까지 다시 표시해줍니다.");
        player.sendMessage(" /inva filter <닉네임> <로그타입> <기준일> - 기준일로부터 지금까지 해당 유저의 특정 로그 기록을 검색합니다.");
        player.sendMessage(" /inva merge - 검색된 로그에서 중복된 인벤토리 로그를 생략합니다.");
        player.sendMessage(" /inva open <인덱스> - inva inspect 이후 검색된 쿼리에서 당시 유저의 인벤토리를 열람합니다.");
        player.sendMessage(" /inva see <유저닉네임> - 해당 유저의 \"현재\" 인벤토리를 확인합니다. ");
        player.sendMessage(" /inva modify <유저닉네임> - 해당 유저의 \"현재\" 인벤토리를 확인후 수정합니다. ");
        player.sendMessage("");
    }
}
