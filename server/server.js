var express = require( "express" );
var app = express();
var db = require( "./js/db_adapter.js" );
var q = require( "q" );

// Handle inbound image requests
app.get( '/img/*', function( req, res ){
    res.sendFile( __dirname + req.url );

});


// Handle inbound content requests
app.post( '/rest', function( req, res ){
    // Wait on the JSON data from this request
    req.on( 'data', function( body ){
        // Attempt to parse the body of the request into
        // JSON. If we got a malformed request, fail out
        // gracefully and tell the client
        try{
            body = JSON.parse( body );
        }catch( e ){
            var data = new Object( );
            data.content = "ERR";
            res.send( JSON.stringify( data ) );
        }

        if( body.reqType == "favorites" ){
            db.userName2Id( body.userId ).then( function( id ){
                db.getUserFavorites( id.id ).then( function( favs ){
                    p_items = favs.map( function( elem ){
                        return db.getItemById( elem.item );
                    });
                    q.all( p_items ).then( function( items ){
                        items = items.map( function( i ){
                            i.photo = "/img/" + i.identifier + ".jpg";
                            delete i.identifier;
                            return i;
                        });
                        res.send( JSON.stringify( items ) );
                    });
                });
            });
        }else if( body.reqType == "nearby" ){

        }else if( body.reqType == "category" ){
            console.log( "c");
            db.getItemsByCategory( body.category, body.count ).then( function( items ){
                items = items.map( function( i ){
                    i.photo = "/img/" + i.identifier + ".jpg";
                    delete i.identifier;
                    return i;
                });
                res.send( JSON.stringify( items ) );
            });
        }


    });
});




// Set up the server
db.initializeSchema( );
console.log( "\n\n ^^ Just ignore that\n" );
// Launch the server
var port = process.env.PORT || 5000;
app.listen( port, function( ) {

    console.log( "Listening on " + port );
});
