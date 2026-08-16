package xyz.busterbrown1218.housingcreativetab.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.Identifier;

public class ClientIdentifierArgumentType implements ArgumentType<Identifier> {
    public ClientIdentifierArgumentType() {}

    public static ClientIdentifierArgumentType identifier() {
        return new ClientIdentifierArgumentType();
    }

    @Override
    public Identifier parse(StringReader reader) throws CommandSyntaxException {
        return Identifier.fromCommandInput(reader);
    }

    public static Identifier getIdentifier(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Identifier.class);
    }
}
