# clallery

A small tryout to replace an existing and obsolete static picture
gallery generator.

## Usage

```
    Clallery: generate a static gallery. 

    Switches                     Default  Desc                      
    --------                     -------  ----                      
    -d, --input-dir              .        Directory for source pics 
    -o, --output-dir             out      Directory for output      
    -v, --no-verbose, --verbose  false    Verbose                   
    -s, --size                   1280x    Picture size [% or px]    
    -h, --no-help, --help        false    This help    
```

## Example run

Make a stupidly simple gallery from the pictures in
`~/pics/digikuvat/current/` and place it in `./out/`. Be verbose about
it.

```
   lein run -- -d ~/pics/digikuvat/current/ -o out -v
```


## Future work

New commits and refactoring will ensue when I have to make a new
gallery page. Probably not too soon.
  

## License

Copyright © 2013 Mikael Puhakka

Distributed under the Eclipse Public License, the same as Clojure.
