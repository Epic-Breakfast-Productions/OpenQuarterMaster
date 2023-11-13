#ifndef MSS_HIGHLIGHT_BLOCKS_COMMAND_H
#define MSS_HIGHLIGHT_BLOCKS_COMMAND_H

#include "MssCommand.h"





class HighlightBlocksCommand : public Command {
private:
    unsigned int duration = 30;
protected:

public:
    HighlightBlocksCommand(
            unsigned int duration
    ) : Command(HIGHLIGHT_BLOCKS) {
        this->duration = duration;
    }

    unsigned int getDuration() {
        return this->duration;
    }
};

#endif
