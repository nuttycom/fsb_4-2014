{-# LANGUAGE OverloadedStrings #-}

module IP where

import Control.Applicative
import Data.Attoparsec.Char8
import Data.ByteString
import Data.Word

data IP = IP Word8 Word8 Word8 Word8 deriving Show

ipParser :: Parser IP
ipParser = 
  IP <$> decimal 
     <*> (char '.' >> decimal)
     <*> (char '.' >> decimal)
     <*> (char '.' >> decimal)

parseIP :: ByteString -> Maybe IP
parseIP = maybeResult . parse ipParser
  
