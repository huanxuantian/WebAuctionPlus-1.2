package me.lorenzop.webauctionplus.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import me.lorenzop.webauctionplus.WebAuctionPlus;

public class MySQLTables {

	protected final String dbPrefix;
	protected final DataQueries dataQueries;

	protected final WebAuctionPlus plugin;
	private boolean isOk;

	public MySQLTables(WebAuctionPlus plugin) {
		this.plugin = plugin;
		this.dataQueries = WebAuctionPlus.dataQueries;
		this.dbPrefix = this.dataQueries.dbPrefix;
		this.isOk = false;

		// create new tables
		sqlTables("Auctions");
		sqlTables("Items");
//		sqlTables("MarketPrices");
		sqlTables("Players");
		sqlTables("RecentSigns");
//		sqlTables("SellPrice");
		sqlTables("ShoutSigns");
		sqlTables("Settings");
		sqlTables("LogSales");
		sqlTables("ServerShops");

		// update existing tables from original web auction
		if(!tableExists("ItemEnchantments") && tableExists("EnchantLinks")) {
			sqlTables("ItemEnchantments");
			// convert database tables to Plus
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "**************************************");
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "*** Converting database to Plus... ***");
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "**************************************");
			ConvertDatabase1_0();
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Finished converting database to Plus!");
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "*** You can delete these tables from the database: EnchantLinks, Enchantments, Mail ***");
		} else
			sqlTables("ItemEnchantments");

		// update 1.0 to 1.1
		if(setColumnExists("Players",   "Locked",       "tinyint(1)   DEFAULT '0'") ) {
			setColumnExists("Auctions", "enchantments", "varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
			setColumnExists("Items",    "enchantments", "varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
			ConvertDatabase1_1_1();
		}
		setColumnExists("Auctions", "itemTitle", "varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
		setColumnExists("Items",    "itemTitle", "varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");

		this.isOk = true;
	}
	public boolean isOk() {return this.isOk;}

	// table queries
	private void sqlTables(String tableName) {
		sqlTables(false, tableName);
	}
	private void sqlTables(boolean alter, String tableName) {
		boolean sucess = false;
		if(alter)
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: sqlTables " + (alter?"Alter":"Create") + " " + tableName);
		// auctions
		if (tableName.equals("Auctions")) {
			if (alter) {
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `player`        `playerName`    VARCHAR(16)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `name`          `itemId`        INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `damage`        `itemDamage`    INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `quantity`      `qty`           INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `price`         `price`         DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `created`       `created`       DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `allowBids`     `allowBids`     TINYINT(1)      NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `currentBid`    `currentBid`    DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions`   CHANGE      `currentWinner` `currentWinner` VARCHAR(16)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
			} else {
				setTableExists("Auctions",
					"`id`               INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`playerId`         INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemId`           INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemDamage`       INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemData`         TEXT            NULL        DEFAULT NULL    , " +
					"`qty`              INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`enchantments`     VARCHAR(255)    NULL        DEFAULT NULL    , " +
					"`price`            DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
					"`created`          DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00', " +
					"`allowBids`        TINYINT(1)      NOT NULL    DEFAULT '0'     , " +
					"`currentBid`       DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
					"`currentWinner`    VARCHAR(16)     NULL        DEFAULT NULL    ");
			}
		} else
		// Items
		if (tableName.equals("Items")) {
			if (alter) {
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Items`      CHANGE      `player`        `playerName`    VARCHAR(16)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Items`      CHANGE      `name`          `itemId`        INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Items`      CHANGE      `damage`        `itemDamage`    INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Items`      CHANGE      `quantity`      `qty`           INT    (11)     NOT NULL    DEFAULT '0'");
			} else {
				setTableExists("Items",
					"`id`           INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`playerId`     INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemId`       INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemDamage`   INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemData`     TEXT            NULL        DEFAULT NULL    , " +
					"`qty`          INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`enchantments` VARCHAR(255)    NULL        DEFAULT NULL    , " +
					"`itemTitle`    VARCHAR(32)     NULL        DEFAULT NULL    ");
			}
		} else
//		// MarketPrices
//		if (tableName.equals("MarketPrices")) {
//			if (alter) {
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"MarketPrices` CHANGE    `name`          `itemId`        INT    (11)     NOT NULL    DEFAULT '0'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"MarketPrices` CHANGE    `damage`        `itemDamage`    INT    (11)     NOT NULL    DEFAULT '0'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"MarketPrices` CHANGE    `time`          `time`          DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"MarketPrices` CHANGE    `marketprice`   `marketprice`   DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"MarketPrices` CHANGE    `ref`           `ref`           INT    (11)     NOT NULL    DEFAULT '0'");
//			} else {
//				setTableExists("MarketPrices",
//					"`id`           INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
//					"`itemId`       INT    (11)     NOT NULL    DEFAULT '0'     , " +
//					"`itemDamage`   INT    (11)     NOT NULL    DEFAULT '0'     , " +
//					"`time`         DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00', " +
//					"`marketprice`  DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
//					"`ref`          INT    (11)     NOT NULL    DEFAULT '0'     ");
//			}
//		} else
		// LogSales
		if (tableName.equals("LogSales")) {
			if (alter) {
				WebAuctionPlus.log.severe("Shouldn't run this!");
			} else {
				sucess  = setTableExists("LogSales",
					"`id`           INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`logType`      ENUM   ('', 'new','sale','cancel')          NULL            DEFAULT NULL    , " +
					"`saleType`     ENUM   ('', 'buynow','auction','server')    NULL            DEFAULT NULL    , " +
					"`timestamp`    DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00'                   , " +
					"`itemType`     ENUM   ('', 'tool','map','book')            NULL            DEFAULT NULL    , " +
					"`itemId`       INT    (11)     NOT NULL    DEFAULT 0       , " +
					"`itemDamage`   INT    (11)     NOT NULL    DEFAULT 0       , " +
					"`enchantments` VARCHAR(255)    NULL        DEFAULT NULL    , " +
					"`itemTitle`    VARCHAR(32)     NULL        DEFAULT NULL    , " +
					"`sellerId`     INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`buyerId`      INT    (11)     NULL        DEFAULT NULL    , " +
					"`qty`          INT    (11)     NOT NULL    DEFAULT 0       , " +
					"`price`        DECIMAL(11,2)   NOT NULL    DEFAULT 0.00    , " +
					"`alert`        TINYINT(1)      NOT NULL    DEFAULT 0       ");
				if(sucess){
					executeRawSQL("ALTER TABLE `"+this.dbPrefix+"LogSales` ADD CONSTRAINT `"+this.dbPrefix+"_fk_seller_id1` FOREIGN KEY (`sellerId`) REFERENCES `"+this.dbPrefix+"Players` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION");
					executeRawSQL("ALTER TABLE `"+this.dbPrefix+"LogSales` ADD CONSTRAINT `"+this.dbPrefix+"_fk_buyer_id1`  FOREIGN KEY (`buyerId`)  REFERENCES `"+this.dbPrefix+"Players` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION");
				}
			}
		} else
		// Players
		if (tableName.equals("Players")) {
			if (alter) {
				executeRawSQL("ALTER TABLE        `"+this.dbPrefix+"Players`  CHANGE  `name`          `playerName`    VARCHAR(16)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
				executeRawSQL("ALTER TABLE        `"+this.dbPrefix+"Players`  CHANGE  `pass`          `password`      VARCHAR(32)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
				executeRawSQL("ALTER TABLE        `"+this.dbPrefix+"Players`  CHANGE  `money`         `money`         DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
				executeRawSQL("ALTER IGNORE TABLE `"+this.dbPrefix+"Players`  CHANGE  `itemsSold`     `itemsSold`     INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER IGNORE TABLE `"+this.dbPrefix+"Players`  CHANGE  `itemsBought`   `itemsBought`   INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER IGNORE TABLE `"+this.dbPrefix+"Players`  CHANGE  `earnt`         `earnt`         DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
				executeRawSQL("ALTER IGNORE TABLE `"+this.dbPrefix+"Players`  CHANGE  `spent`         `spent`         DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
				executeRawSQL("ALTER TABLE        `"+this.dbPrefix+"Players`  ADD     `Permissions`   SET( '', 'canBuy', 'canSell', 'isAdmin' )   NULL    DEFAULT NULL");
			} else {
				sucess = setTableExists("Players",
					"`id`           INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`playerName`   VARCHAR(16)     NULL        DEFAULT NULL    , " +
					"`uuid`         VARCHAR(50)     NULL        DEFAULT NULL    , " +
					"`password`     VARCHAR(32)     NULL        DEFAULT NULL    , " +
					"`money`        DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
					"`itemsSold`    INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`itemsBought`  INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`earnt`        DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
					"`spent`        DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
					"`Permissions`  SET( '', 'canBuy', 'canSell', 'isAdmin' )   NULL DEFAULT NULL ," +
					"`Locked`       TINYINT(1)                                  NOT NULL	DEFAULT '0'		");
				if(sucess){
					executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Auctions` ADD CONSTRAINT `"+this.dbPrefix+"_fk_player_id1` FOREIGN KEY (`playerId`) REFERENCES `"+this.dbPrefix+"Players` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION");
					executeRawSQL("ALTER TABLE `"+this.dbPrefix+"Items`    ADD CONSTRAINT `"+this.dbPrefix+"_fk_player_id2` FOREIGN KEY (`playerId`) REFERENCES `"+this.dbPrefix+"Players` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION");
				}
			}
		} else
		// RecentSigns
		if (tableName.equals("RecentSigns")) {
			if (alter) {
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"RecentSigns` CHANGE     `world`     `world`     VARCHAR(32)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"RecentSigns` CHANGE     `offset`    `offset`    INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"RecentSigns` CHANGE     `x`         `x`         INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"RecentSigns` CHANGE     `y`         `y`         INT    (11)     NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"RecentSigns` CHANGE     `z`         `z`         INT    (11)     NOT NULL    DEFAULT '0'");
			} else {
				setTableExists("RecentSigns",
					"`id`       INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`world`    VARCHAR(32)     NULL        DEFAULT NULL    , " +
					"`offset`   INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`x`        INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`y`        INT    (11)     NOT NULL    DEFAULT '0'     , " +
					"`z`        INT    (11)     NOT NULL    DEFAULT '0'     ");
			}
		} else
//		// SellPrice
//		if (tableName.equals("SellPrice"))
//			if (alter) {
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `name`      `itemId`        INT    (11)     NOT NULL    DEFAULT '0'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `damage`    `itemDamage`    INT    (11)     NOT NULL    DEFAULT '0'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `time`      `time`          DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `quantity`  `qty`           INT    (11)     NOT NULL    DEFAULT '0'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `price`     `price`         DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `seller`    `seller`        VARCHAR(16)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
//				executeRawSQL("ALTER TABLE `"+dbPrefix+"SellPrice`  CHANGE  `buyer`     `buyer`         VARCHAR(16)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
//			} else {
//				setTableExists("SellPrice",
//					"`id`           INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
//					"`itemId`       INT    (11)     NOT NULL    DEFAULT '0'     , " +
//					"`itemDamage`   INT    (11)     NOT NULL    DEFAULT '0'     , " +
//					"`time`         DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00', " +
//					"`qty`          INT    (11)     NOT NULL    DEFAULT '0'     , " +
//					"`price`        DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
//					"`seller`       VARCHAR(16)     NULL        DEFAULT NULL    , " +
//					"`buyer`        VARCHAR(16)     NULL        DEFAULT NULL    ");
//			}
//		} else
		// Settings
		if (tableName.equals("Settings")) {
			if (alter) {
				WebAuctionPlus.log.severe("Shouldn't run this!");
			} else {
				setTableExists("Settings",
					"`id`       INT    (11)     NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`name`     VARCHAR(32)     NULL        DEFAULT NULL    , UNIQUE(`name`)   , " +
					"`value`    VARCHAR(255)    NULL        DEFAULT NULL    ");
			}
		} else
		// ShoutSigns
		if (tableName.equals("ShoutSigns")) {
			if (alter) {
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"ShoutSigns` CHANGE  `world`     `world`     VARCHAR(32)     CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"ShoutSigns` CHANGE  `radius`    `radius`    INT(11)         NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"ShoutSigns` CHANGE  `x`         `x`         INT(11)         NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"ShoutSigns` CHANGE  `y`         `y`         INT(11)         NOT NULL    DEFAULT '0'");
				executeRawSQL("ALTER TABLE `"+this.dbPrefix+"ShoutSigns` CHANGE  `z`         `z`         INT(11)         NOT NULL    DEFAULT '0'");
			} else {
				setTableExists("ShoutSigns",
					"`id`       INT(11)         NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
					"`world`    VARCHAR(32)     NULL        DEFAULT NULL    , " +
					"`radius`   INT(11)         NOT NULL    DEFAULT '0'     , " +
					"`x`        INT(11)         NOT NULL    DEFAULT '0'     , " +
					"`y`        INT(11)         NOT NULL    DEFAULT '0'     , " +
					"`z`        INT(11)         NOT NULL    DEFAULT '0'     ");
			}
		} else
		// ServerShops
		if (tableName.equals("ServerShops")) {
			if (alter) {
			} else {
				setTableExists("ServerShops",
						"`id`               INT(11)         NOT NULL    AUTO_INCREMENT  , PRIMARY KEY(`id`), " +
						"`itemId`           INT(11)         NOT NULL    DEFAULT '0'     , " +
						"`itemDamage`       INT(11)         NOT NULL    DEFAULT '0'     , " +
						"`itemData`         TEXT            NULL        DEFAULT NULL    , " +
						"`qty`              INT(11)         NOT NULL    DEFAULT '0'     , " +
						"`enchantments`     VARCHAR(255)    NULL        DEFAULT NULL    , " +
						"`priceBuy`         DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
						"`priceSell`        DECIMAL(11,2)   NOT NULL    DEFAULT '0.00'  , " +
						"`created`          DATETIME        NOT NULL    DEFAULT '0000-00-00 00:00:00' , " +
						"`itemTitle`        VARCHAR(32)     NULL        DEFAULT NULL    ");
			}
		}
	}

	// convert database tables to Plus
	@SuppressWarnings("resource")
	private void ConvertDatabase1_0() {
		// update tables
		sqlTables(true, "Auctions");
		sqlTables(true, "Items");
		sqlTables(true, "MarketPrices");
		sqlTables(true, "Players");
		sqlTables(true, "RecentSigns");
		sqlTables(true, "SellPrice");
		sqlTables(true, "ShoutSigns");

		Connection conn         = getConnection();
		PreparedStatement st    = null;
		PreparedStatement stNew = null;
		ResultSet rs            = null;
		ResultSet rs2           = null;

		// check if already updated
		try {
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Count Database Settings");
			st = conn.prepareStatement("SELECT COUNT(*) AS `count` FROM `"+this.dbPrefix+"Settings`");
			rs = st.executeQuery();
			if (!rs.next()) {
				WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix + "Could not get total settings!");
				return;
			}
			if (rs.getInt(1) != 0) {
				WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix + "ERROR!! ALREADY CONVERTED DATABASE!!");
				return;
			}
			// add a setting
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Insert Version Setting");
			st = conn.prepareStatement("INSERT INTO `"+this.dbPrefix+"Settings` (`name`,`value`) VALUES ('Version',?)");
			st.setString(1, this.plugin.getDescription().getVersion().toString());
			st.executeUpdate();
		} catch (SQLException e) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to update Players table!");
			e.printStackTrace();
		} finally {
			closeResources(st, rs);
			closeResources(stNew, rs2);
		}

		// update player permissions
		if (tableExists("Players")) {
			int countPlayers = 0;
			int totalPlayers = 0;
			st    = null;
			stNew = null;
			rs    = null;
			rs2   = null;
			try {
				if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Convert Database Players");
				// get total players
				st = conn.prepareStatement("SELECT COUNT(*) AS `count` FROM `"+this.dbPrefix+"Players`");
				rs = st.executeQuery();
				if (!rs.next()) {
					WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix + "Could not get total players!");
					return;
				}
				totalPlayers = rs.getInt(1);
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Found " + Integer.toString(totalPlayers) + " player accounts");
				// get old players permissions
				st = conn.prepareStatement("SELECT `id`,`canBuy`,`canSell`,`isAdmin` FROM `"+this.dbPrefix+"Players`");
				rs = st.executeQuery();
				String tempPerms = "";
				while (rs.next()) {
					stNew = conn.prepareStatement("UPDATE `"+this.dbPrefix+"Players` SET `Permissions` = ? WHERE `id` = ?");
					tempPerms = "";
					if (rs.getBoolean("canBuy"))  tempPerms = WebAuctionPlus.addStringSet(tempPerms, "canBuy",  ",");
					if (rs.getBoolean("canSell")) tempPerms = WebAuctionPlus.addStringSet(tempPerms, "canSell", ",");
					if (rs.getBoolean("isAdmin")) tempPerms = WebAuctionPlus.addStringSet(tempPerms, "isAdmin", ",");
					stNew.setString(1, tempPerms);
					stNew.setInt   (2, rs.getInt("id"));
					stNew.executeUpdate();
					countPlayers++;
					if(totalPlayers > 500) WebAuctionPlus.PrintProgress(countPlayers, totalPlayers);
				}
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Converted " + Integer.toString(countPlayers) + " player accounts");
			} catch (SQLException e) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to update Players table!");
				e.printStackTrace();
			} finally {
				closeResources(st, rs);
				closeResources(stNew, rs2);
			}
		}
		// move mail to items table
		if (tableExists("Mail")) {
			int countMail = 0;
			int totalMail = 0;
			st    = null;
			stNew = null;
			rs    = null;
			rs2   = null;
			try {
				if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Convert Database Mail");
				// get total mail
				st = conn.prepareStatement("SELECT COUNT(*) AS `count` FROM `"+this.dbPrefix+"Mail`");
				rs = st.executeQuery();
				if (!rs.next()) {
					WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix + "Could not get total mail!");
					return;
				}
				totalMail = rs.getInt(1);
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Found " + Integer.toString(totalMail) + " mail stacks");
				// get old mail items
				st = conn.prepareStatement("SELECT `name`,`damage`,`player`,`quantity` FROM `"+this.dbPrefix+"Mail`");
				rs = st.executeQuery();
				while (rs.next()) {
					stNew = conn.prepareStatement("INSERT INTO `"+this.dbPrefix+"Items` " +
						"(`ItemTable`,`itemId`,`itemDamage`,`playerName`,`qty`) VALUES ('Mail',?,?,?,?)");
					stNew.setInt   (1, rs.getInt   ("name"));
					stNew.setInt   (2, rs.getInt   ("damage"));
					stNew.setString(3, rs.getString("player"));
					stNew.setInt   (4, rs.getInt   ("quantity"));
					stNew.executeUpdate();
					countMail++;
					if(totalMail > 500) WebAuctionPlus.PrintProgress(countMail, totalMail);
				}
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Converted " + Integer.toString(countMail) + " mail stacks");
			} catch (SQLException e) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to update Mail table");
				e.printStackTrace();
			} finally {
				closeResources(st, rs);
				closeResources(stNew, rs2);
			}
			// make sure nothing's null
			executeRawSQL("UPDATE `"+this.dbPrefix+"Items` SET `ItemTable`='Items' WHERE `ItemTable` IS NULL");
		}
		// move data to new ItemEnchantments table
		if (tableExists("EnchantLinks")) {
			int countEnchantments = 0;
			int totalEnchantments = 0;
			st    = null;
			stNew = null;
			rs    = null;
			rs2   = null;
			try {
				if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Convert Database Enchantments");
				// get total enchantments
				st = conn.prepareStatement("SELECT COUNT(*) AS `count` FROM `"+this.dbPrefix+"EnchantLinks`");
				rs = st.executeQuery();
				if (!rs.next()) {
					WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix + "Could not get total enchantments!");
					return;
				}
				totalEnchantments = rs.getInt(1);
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Found " + Integer.toString(totalEnchantments) + " enchantments");
				// get old enchantments table
				st = conn.prepareStatement("SELECT `enchId`,`itemTableId`,`itemId` " +
					"FROM `"+this.dbPrefix+"EnchantLinks` ORDER BY `itemId` ASC");
				rs = st.executeQuery();
				while (rs.next()) {
					stNew = conn.prepareStatement("INSERT INTO `"+this.dbPrefix+"ItemEnchantments` (" +
						"`ItemTable`, `ItemTableId`, `enchName`, `enchId`, `level`) VALUES (" +
						"?, ?, ?, ?, ? )");
					// ItemTable Enum
					if        (rs.getInt("itemTableId") == 0) {
						stNew.setString(1, "Items");
					} else if (rs.getInt("itemTableId") == 1) {
						stNew.setString(1, "Auctions");
					} else if (rs.getInt("itemTableId") == 2) {
						stNew.setString(1, "Mail");
					} else {
						stNew.setString(1, "Items");
					}
					// ItemTableId
					stNew.setInt(2, rs.getInt("itemId"));
					// query Enchantments (old table)
					st = conn.prepareStatement("SELECT `enchName`, `enchId`, `level` " +
						"FROM `"+this.dbPrefix+"Enchantments` WHERE `id` = ?");
					st.setInt(1, rs.getInt("enchId"));
					rs2 = st.executeQuery();
					if (rs2.next()) {
						// enchName
						stNew.setString(3, rs2.getString("enchName"));
						// enchId
						stNew.setInt   (4, rs2.getInt   ("enchId"));
						// level
						stNew.setInt   (5, rs2.getInt   ("level"));
					} else {
						stNew.setString(3, null);
						stNew.setInt   (4, 0);
						stNew.setInt   (5, 0);
					}
					stNew.executeUpdate();
					countEnchantments++;
					if(totalEnchantments > 500) WebAuctionPlus.PrintProgress(countEnchantments, totalEnchantments);
				}
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Converted " + Integer.toString(countEnchantments) + " enchantments");
			} catch (SQLException e) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to update Enchantments table");
				e.printStackTrace();
			} finally {
				closeResources(st, rs);
				closeResources(stNew, rs2);
			}
		}
		closeResources(conn);
	}


	// update from 1.0 to 1.1.1
	@SuppressWarnings("resource")
	private void ConvertDatabase1_1_1() {
		Connection conn         = getConnection();
		PreparedStatement st    = null;
		PreparedStatement stNew = null;
		ResultSet rs            = null;
		ResultSet rs2           = null;

		if(tableExists("ItemEnchantments")) {
			int totalItemEnchantments = 0;
			int countEnchantments = 0;
			st    = null;
			stNew = null;
			rs    = null;
			rs2   = null;
			try {
				if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: Convert ItemEnchantments");
				// get total enchantments
				st = conn.prepareStatement("SELECT COUNT(*) AS `count` FROM `"+this.dbPrefix+"ItemEnchantments`");
				rs = st.executeQuery();
				if (!rs.next()) {
					WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix + "Could not get total item enchantments!");
					return;
				}
				totalItemEnchantments = rs.getInt(1);
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Found " + Integer.toString(totalItemEnchantments) + " item enchantments");
				// get old item enchantments
				st = conn.prepareStatement("SELECT `ItemTable`, `ItemTableId`, `enchId`, `level`  FROM `"+this.dbPrefix+"ItemEnchantments` ORDER BY `enchId` ASC");
				rs = st.executeQuery();
				HashMap<Integer, String> enchTempAuctions = new HashMap<Integer, String>();
				HashMap<Integer, String> enchTempItems    = new HashMap<Integer, String>();
				while (rs.next()) {
					// auctions table
					if(rs.getString("ItemTable").equals("Auctions")) {
						// add enchantment string
						String enchStr = "";
						if(enchTempAuctions.containsKey( rs.getInt("ItemTableId") )) enchStr = enchTempAuctions.get( rs.getInt("ItemTableId") )+",";
						enchStr += Integer.toString(rs.getInt("enchId"))+":"+Integer.toString(rs.getInt("level"));
						enchTempAuctions.put(rs.getInt("ItemTableId"), enchStr);
						// update row
						stNew = conn.prepareStatement("UPDATE `"+this.dbPrefix+"Auctions` SET `enchantments` = ? WHERE `id` = ? LIMIT 1");
						stNew.setString(1, enchStr);
						stNew.setInt   (2, rs.getInt("ItemTableId"));
					// items table
					} else {
						// add enchantment string
						String enchStr = "";
						if(enchTempItems.containsKey( rs.getInt("ItemTableId") )) enchStr = enchTempItems.get( rs.getInt("ItemTableId") )+",";
						enchStr += Integer.toString(rs.getInt("enchId"))+":"+Integer.toString(rs.getInt("level"));
						enchTempItems.put(rs.getInt("ItemTableId"), enchStr);
						// update row
						stNew = conn.prepareStatement("UPDATE `"+this.dbPrefix+"Items` SET `enchantments` = ? WHERE `id` = ? LIMIT 1");
						stNew.setString(1, enchStr);
						stNew.setInt   (2, rs.getInt("ItemTableId"));
					}
					stNew.executeUpdate();
					countEnchantments++;
					if(totalItemEnchantments > 500) WebAuctionPlus.PrintProgress(countEnchantments, totalItemEnchantments);
				}
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Converted " + Integer.toString(countEnchantments) + " enchantments for "+
					Integer.toString(enchTempAuctions.size()+enchTempItems.size())+" items");
			} catch (SQLException e) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to convert enchantments");
				e.printStackTrace();
			} finally {
				closeResources(st, rs);
				closeResources(stNew, rs2);
			}
		}
		closeResources(conn);
	}


	public Connection getConnection() {
		return this.dataQueries.getConnection();
	}
	public void closeResources(Connection conn, Statement st, ResultSet rs) {
		this.dataQueries.closeResources(conn, st, rs);
	}
	public void closeResources(Connection conn) {
		this.dataQueries.closeResources(conn);
	}
	public static void closeResources(Statement st, ResultSet rs) {
		DataQueries.closeResources(st, rs);
	}
	public void executeRawSQL(String sql) {
		this.dataQueries.executeRawSQL(sql);
	}
	protected boolean tableExists(String tableName) {
		return this.dataQueries.tableExists(tableName);
	}
	protected boolean setTableExists(String tableName, String Sql) {
		return this.dataQueries.setTableExists(tableName, Sql);
	}
	protected boolean columnExists(String tableName, String columnName) {
		return this.dataQueries.columnExists(tableName, columnName);
	}
	protected boolean setColumnExists(String tableName, String columnName, String Attr) {
		return this.dataQueries.setColumnExists(tableName, columnName, Attr);
	}


}
