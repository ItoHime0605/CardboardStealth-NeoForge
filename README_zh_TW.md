# Cardboard Stealth／紙箱潛行 — 0.1.1-alpha 開發原始碼

> **尚未完成 NeoForge 編譯或 Minecraft 專用伺服器實測。** 本工程是原始碼，不是可直接安裝的模組 JAR。

環境：Minecraft 1.21.1、NeoForge 21.1.x、Java 21、Curios 9.5.1、Zombie Awareness 1.13.2。此工程不需要 Zombie Extreme。

## 0.1.1 實作變更

1. **靜止／移動兩段識破半徑（新版需求）**
   - 紙箱內完全靜止：殭屍在 **1.25 格（含）** 內能識破；距離 **超過 1.25 格** 時不會因直接辨識選擇新目標。
   - 紙箱內移動：殭屍在 **3.5 格（含）** 內能識破；距離 **超過 3.5 格** 時不會因直接辨識選擇新目標。
   - 判斷使用三維距離平方；距離相等時視為可識破，避免邊界不明確。
   - 伺服器每個玩家 tick 記錄本人的位移，不需要每 tick 掃描附近全部殭屍。忽略小於 0.01 格的座標誤差；停止移動後再經過約 2 tick 才判定靜止，跳躍期間視為移動。
   - 已鎖定玩家的殭屍**不會**因為距離超過識破半徑而被清除目標。
2. **保留原本殭屍仇恨／搜尋**
   - 不再強制對附近殭屍 `setTarget(null)` 或 `navigation.stop()`。
   - 玩家啟用紙箱前，確認是否有符合條件的殭屍仍將自己作為目標；沒有真正脫離追擊就不能利用紙箱重入潛行。
   - Zombie Awareness 自訂 `ZAUtil.isEnemy` 和 NeoForge 新目標事件使用相同識破規則。
   - 失去目標後的原有循聲、循氣味搜尋路徑不會被整批刪除。
3. **移動物理**
   - 進入紙箱時賦予 `-40%` 移動速度屬性修正，離開即移除。
   - 蹲行可沿用原版跨越半格障礙；跳躍初速限制為 `0.245`，設計目標為約半格高的低跳。
   - 跳躍產生小量 Zombie Awareness 聲音線索，原本的腳步、落地音量按比例降低。
4. **既有原型保留**
   - Curios 紙箱槽、蹲住 20 tick 啟動、放開蹲下結束；網路同步；上身紙箱渲染原型；中文名稱與配方。
   - 現有三種箱子的像素材質只是**舊的暫用原型**，正式 3D 外觀及上半磚向後傾斜動畫**尚未製作**，等使用者提供圖片再做。
   - `BoxDetectionRules` 為可獨立測試的純 Java 判定器。

## 重要限制與後續工作

- **尚未編譯成功／未執行遊戲測試**：本次只完成原始碼及 9 項純 Java 距離規則測試，不能宣稱在 NeoForge 中已可運作。
- Zombie Awareness 的音效追蹤掛鉤以**座標**而非來源玩家做判定；現有 0.1.0 的腳步抑制仍可能在玩家貼近時錯誤影響其他玩家。**必須在下一個版本改為來源歸屬明確的方案，或實測隔離。**
- 半格跨越和半格跳躍高度，目前是依據原版移動模型的程式設計值，需要遊戲實測再校準，不保證其他移動／重力模組下也恰好半格。
- 目前 Zombie Awareness 跟 `fooled_by_box` 標籤支援的怪物能介入索敵；未來更換殭屍模組時需要額外對接它的 AI。
- 仍需完成槍械開火中斷、玩家死亡／登出、原版與特殊怪物追蹤、多人同步、Curios 槽位，以及裝甲／手持物件的相容實測。

## 程式距離規則的本機測試（不依賴遊戲）

在專案根目錄執行：

```sh
mkdir -p /tmp/cardboard-rules
javac -encoding UTF-8 -d /tmp/cardboard-rules \
  src/main/java/tw/zonezero/cardboardstealth/BoxDetectionRules.java \
  tests/BoxDetectionRulesTest.java
java -cp /tmp/cardboard-rules BoxDetectionRulesTest
```

預期結果：`PASS: 9/9 detection boundary checks`。

## NeoForge 專案建置

此 ZIP 不包含 Zombie Awareness／Curios 的 JAR，也不含 Gradle Wrapper。準備 JDK 21 與 Gradle 8.10.2 後，在有網路並可連接 NeoForge 與依賴倉庫的環境執行：

```sh
gradle --no-daemon build
```

理論輸出檔名：`cardboardstealth-neoforge-1.21.1-0.1.1-alpha.jar`，**只有真正編譯成功後**才可用於測試伺服器。GitHub Actions 工作流程也會使用這個版本名稱。
