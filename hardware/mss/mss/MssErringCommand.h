#ifndef MSS_ERRING_COMMAND_H
#define MSS_ERRING_COMMAND_H

#include "MssCommand.h"

class ErringCommand : public Command {
private:
    const __FlashStringHelper *description;
protected:
    ErringCommand(
            CommandType command,
            const __FlashStringHelper *description
    ) : Command(command) {
        this->description = description;
    }

public:
    const __FlashStringHelper *getDescription() {
        return this->description;
    }
};

class RequestCommandError : public ErringCommand {
public:
    RequestCommandError(const __FlashStringHelper *description) :
            ErringCommand(CommandType::REQUEST_ERROR, description) {
    }
};


#endif
