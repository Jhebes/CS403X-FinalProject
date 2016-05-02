var fs = require( "fs" );
var file = "trendly.db";
var exists = fs.existsSync( file );
var sqlite3 = require( "sqlite3" );
var db = new sqlite3.Database( file );
var crypto = require('crypto');
var q = require( "q" );

var hash = function( str ){
    shasum = crypto.createHash('sha1');
    shasum.update( str );
    return shasum.digest( 'hex' );
};


module.exports = {

    initializeSchema: function( ){
        if( !exists ){
            db.serialize( function( ) {
                db.run( "CREATE TABLE users ("
                        +   "id TEXT NOT NULL PRIMARY KEY,"
                        +   "username TEXT NOT NULL"
                        +   ")"
                );

                db.run( "CREATE TABLE favorites ("
                        +   "owner TEXT NOT NULL,"
                        +   "item TEXT NOT NULL,"
                        +   "PRIMARY KEY (owner, item),"
                        +   "FOREIGN KEY (item) REFERENCES clothing(identifier)"
                        +   ")"
                );

                db.run( "CREATE TABLE brand ("
                        +   "name TEXT NOT NULL PRIMARY KEY,"
                        +   "premium INTEGER NOT NULL,"
                        +   "location TEXT NOT NULL"
                        +   ")"
                );

                db.run( "CREATE TABLE clothing ("
                        +   "identifier TEXT NOT NULL PRIMARY KEY," // SHA1 hash of itemName + brandName to ensure uniquensss
                        +   "itemName TEXT NOT NULL,"
                        +   "brandName TEXT NOT NULL,"
                        +   "category TEXT NOT NULL,"
                        +   "extraTags TEXT NOT NULL,"
                        +   "FOREIGN KEY(brandName) REFERENCES brand(name)"
                        +   ")"
                );

                db.run( "CREATE TABLE collections ("
                        +   "collectionName TEXT NOT NULL,"
                        +   "item TEXT NOT NULL,"
                        +   "FOREIGN KEY (item) REFERENCES clothing(identifier),"
                        +   "PRIMARY KEY(collectionName, item)"
                        +   ")"
                );

                db.run( "CREATE TABLE likes ("
                        +   "item TEXT NOT NULL,"
                        +   "lat FLOAT NOT NULL,"
                        +   "lng FLOAT NOT NULL,"
                        +   "FOREIGN KEY item REFERENCES clothing(identifier),"
                        +   "PRIMARY KEY(item, lat, lng)"
                        +   ")"
                );

            });
        };
    },
    userName2Id: function( userName) {
        var p_done = q.defer( );
        db.serialize( function( ) {
            if( !exists ) {
                return null;
            }
            try{
                var stmt = db.prepare("SELECT id FROM users WHERE username LIKE ? LIMIT 1;");
                results = stmt.get( userName, function( err, results ){
                    p_done.resolve( results );
                    stmt.finalize( );
                });
            }catch( e ){
                return null;
            }

        });
        return p_done.promise;
    },
    createUser: function( username ) {
        try{
            var stmt = db.prepare("INSERT INTO users (id, username) VALUES (?,?);");
            stmt.run( hash( username ), username );
            stmt.finalize( );
            return results;
        }catch( e ){
            return null;
        }
    },
    like: function( item, lat, lng ) {
        try{
            var stmt = db.prepare("INSERT INTO likes (item, lat, lng) VALUES (?,?,?);");
            stmt.run( item, lat, lng );
            stmt.finalize( );
            return results;
        }catch( e ){
            return null;
        }
    },
    getItemsByPopular: function( lat, lng, range, cnt ) {
        var p_done = q.defer( );
        db.serialize( function( ) {
            if( !exists ) {
                return null;
            }
            try{
                var stmt = db.prepare("SELECT item,itemName,brandName,category,extraTags FROM (SELECT item,count(*) as cnt FROM likes WHERE (abs(? - lat) + abs(? - lng)) < ? GROUP BY item) JOIN clothing ON item = identifier ORDER BY cnt DESC LIMIT ?;");
                stmt.all( lat, lng, parseInt( range ), cnt, function( err, results ){
                    p_done.resolve( results );
                    stmt.finalize( );
                });
            }catch( e ){
                return null;
            }

        });
        return p_done.promise;

    },
    getItemById: function( itemId ) {
        var p_done = q.defer( );
        db.serialize( function( ) {
            if( !exists ) {
                return null;
            }
            try{
                var stmt = db.prepare("SELECT * FROM clothing WHERE identifier = ? LIMIT 1;");
                results = stmt.get( itemId, function( err, results ){
                    p_done.resolve( results );
                    stmt.finalize( );
                });
            }catch( e ){
                return null;
            }

        });
        return p_done.promise;

    },
    getItemsByCategory: function( category, cnt ) {
        var p_done = q.defer( );
        db.serialize( function( ) {
            if( !exists ) {
                return null;
            }
            try{
                var stmt = db.prepare("SELECT * FROM clothing WHERE category = ? LIMIT ?;");
                results = stmt.all( category, cnt, function( err, results ){
                    p_done.resolve( results );
                    stmt.finalize( );
                });
            }catch( e ){
                return null;
            }

        });
        return p_done.promise;

    },
    getUserFavorites: function( userID, count ) {
        var p_done = q.defer( );
        db.serialize( function( ) {
            if( !exists ) {
                return null;
            }
            try{
                var stmt = db.prepare("SELECT * FROM favorites WHERE owner = ?;");
                stmt.all( userID, count, function( err, results ){
                    p_done.resolve( results );
                    stmt.finalize( );
                });
            }catch( e ){
                return null;
            }

        });
        return p_done.promise;
    }

}
